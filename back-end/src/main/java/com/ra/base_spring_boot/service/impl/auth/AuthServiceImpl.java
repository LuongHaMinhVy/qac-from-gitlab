package com.ra.base_spring_boot.service.impl.auth;

import com.ra.base_spring_boot.dto.req.*;
import com.ra.base_spring_boot.dto.resp.AccountResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.LoginResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpUnAuthorized;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.account.*;
import com.ra.base_spring_boot.repository.user.UserRepo;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.repository.ActivityLogRepo;
import com.ra.base_spring_boot.utils.SecurityUtils;
import com.ra.base_spring_boot.security.principle.MyUserDetailsService;
import com.ra.base_spring_boot.service.auth.AccountActivationService;
import com.ra.base_spring_boot.service.auth.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final IAccountRepository accountRepo;
    private final BlacklistTokenRepo blacklistTokenRepo;
    private final MyUserDetailsService userDetailsService;
    private final IRoleRepository roleRepo;
    private final AccountRoleRepo accountRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserRepo userProfileRepo;
    private final ActivityLogRepo activityLogRepo;
    private final ResetPasswordTokenRepo resetPasswordTokenRepo;
    private final EmailService emailService;
    private final AccountActivationService accountActivationService;

    @Override
    @Transactional
    public ApiResponse<String> register(FormRegister req) {

        if (accountRepo.existsByEmail(req.getEmail())) {
            throw new HttpBadRequest("Email đã tồn tại");
        }

        if (accountRepo.existsByUsername(req.getUsername())) {
            throw new HttpBadRequest("Tên đăng nhập đã tồn tại");
        }

        Account account = Account.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .isActive(false)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        accountRepo.save(account);

        User newUser = User.builder()
                .account(account)
                .fullName(req.getUsername())
                .build();

        userProfileRepo.save(newUser);

        Role role = roleRepo.findByRoleCode(RoleName.ROLE_USER)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy quyền mặc định ROLE_USER"));

        AccountRole accountRole = AccountRole.builder()
                .account(account)
                .role(role)
                .isPrimary(true)
                .build();

        accountRoleRepo.save(accountRole);

        accountActivationService.createActivationToken(account);

        activityLogRepo.save(ActivityLog.builder()
                .account(account)
                .action("REGISTER")
                .details("New user registered with username: " + account.getUsername())
                .createdAt(LocalDateTime.now())
                .build());

        return ApiResponse.success(
                "Đăng ký thành công!",
                "Vui lòng kiểm tra email để nhận mã kích hoạt OTP.");
    }

    @Override
    @Transactional
    public ApiResponse<LoginResponse> login(FormLogin req) {

        Account acc = accountRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new HttpBadRequest("Email hoặc mật khẩu không chính xác"));

        if (acc.getLockUntil() != null && acc.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new HttpForbiden("Tài khoản tạm thời bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau.");
        }

        if (!passwordEncoder.matches(req.getPassword(), acc.getPasswordHash())) {
            Integer attempts = acc.getLoginAttempts() == null ? 1 : acc.getLoginAttempts() + 1;
            acc.setLoginAttempts(attempts);

            if (attempts >= 5) {
                acc.setLockUntil(LocalDateTime.now().plusMinutes(15));
                accountRepo.save(acc);
                throw new HttpForbiden("Đăng nhập sai quá 5 lần. Tài khoản bị khóa 15 phút.");
            }

            accountRepo.save(acc);
            throw new HttpBadRequest("Email hoặc mật khẩu không chính xác. Còn " + (5 - attempts) + " lần thử.");
        }

        if (!acc.getEmailVerified()) {
            throw new HttpForbiden("Vui lòng xác minh email trước khi đăng nhập");
        }

        if (!acc.getIsActive()) {
            throw new HttpForbiden("Tài khoản đã bị khóa");
        }

        acc.setLoginAttempts(0);
        acc.setLockUntil(null);
        acc.setLastLoginAt(LocalDateTime.now());
        acc.setUpdatedAt(LocalDateTime.now());
        accountRepo.save(acc);

        String username = acc.getUsername();

        String accessToken = jwtProvider.generateToken(username);
        String refreshToken = jwtProvider.generateRefreshToken(username);

        List<String> roleCodes = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        if (acc.getRoles() != null) {
            for (Role role : acc.getRoles()) {
                roleCodes.add(role.getRoleCode().name());
                if (role.getRolePermissions() != null) {
                    for (RolePermission rp : role.getRolePermissions()) {
                        if (rp.getPermission() != null) {
                            permissions.add(rp.getPermission().getPermissionCode());
                        }
                    }
                }
            }
        }

        roleCodes = roleCodes.stream().distinct().toList();
        permissions = permissions.stream().distinct().toList();

        AccountResponseDTO accountResponseDTO = AccountResponseDTO.builder()
                .id(acc.getAccountId())
                .email(acc.getEmail())
                .username(acc.getUsername())
                .status(acc.getIsActive())
                .roles(roleCodes)
                .permissions(permissions)
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .account(accountResponseDTO)
                .build();

        activityLogRepo.save(
                ActivityLog.builder()
                        .account(acc)
                        .action("LOGIN")
                        .details("User logged in successfully")
                        .createdAt(LocalDateTime.now())
                        .build());

        return ApiResponse.success(loginResponse, "Đăng nhập thành công");
    }

    @Override
    @Transactional
    public ApiResponse<String> logout(String authorizationHeader) {

        String token = authorizationHeader.replace("Bearer", "").trim();

        if (!StringUtils.hasText(token)) {
            throw new HttpBadRequest("Token không hợp lệ!");
        }

        Date expiry = jwtProvider.extractExpiration(token);

        BlacklistToken blacklist = new BlacklistToken();
        blacklist.setToken(token);
        blacklist.setExpiryDate(expiry.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());

        blacklistTokenRepo.save(blacklist);

        ActivityLog activityLog = new ActivityLog();
        activityLog.setAction("LOGOUT");
        activityLog.setDetails("Token blacklisted until: " + blacklist.getExpiryDate());

        try {
            String username = jwtProvider.extractUsername(token);
            Account account = accountRepo.findByUsername(username).orElse(null);
            activityLog.setAccount(account);
        } catch (Exception ignored) {
            activityLog.setAccount(null);
            activityLog.setDetails(
                    "Logout but could not resolve account. Token blacklisted until: " + blacklist.getExpiryDate());
        }

        activityLogRepo.save(activityLog);

        return ApiResponse.success("Đăng xuất thành công", "Token đã được vô hiệu hóa");
    }

    @Override
    public ApiResponse<Map<String, String>> refreshToken(RefreshTokenRequest req) {
        if (req == null || !StringUtils.hasText(req.getRefreshToken())) {
            throw new HttpBadRequest("Thiếu refresh token");
        }

        String refreshToken = req.getRefreshToken().trim();

        if (blacklistTokenRepo.findByToken(refreshToken).isPresent()) {
            throw new HttpBadRequest("Refresh token đã bị thu hồi");
        }

        String username;
        try {
            username = jwtProvider.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new HttpBadRequest("Refresh token không hợp lệ");
        }

        Account acc = accountRepo.findByUsername(username)
                .orElseThrow(() -> new HttpBadRequest("Refresh token không hợp lệ"));

        if (!acc.getEmailVerified())
            throw new HttpForbiden("Vui lòng xác minh email");
        if (!acc.getIsActive())
            throw new HttpForbiden("Tài khoản đã bị khóa");

        if (!jwtProvider.validateToken(refreshToken, userDetailsService.loadUserByUsername(username))) {
            throw new HttpBadRequest("Refresh token đã hết hạn hoặc không hợp lệ");
        }

        String newAccessToken = jwtProvider.generateToken(username);

        blacklistTokenRepo.save(
                BlacklistToken.builder()
                        .token(refreshToken)
                        .expiryDate(
                                jwtProvider.extractExpiration(refreshToken).toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime())
                        .build());

        return ApiResponse.success(
                Map.of(
                        "accessToken", newAccessToken),
                "Làm mới token thành công");
    }

    @Override
    public ApiResponse<String> activateAccount(ActivateAccountRequest req) {

        accountActivationService.activateAccount(req.getEmail(), req.getOtp());

        return ApiResponse.success(
                "Kích hoạt tài khoản thành công",
                "Bây giờ bạn có thể đăng nhập");
    }

    @Override
    @Transactional
    public ApiResponse<String> changePassword(ChangePasswordRequest req) {
        Account account = SecurityUtils.getCurrentAccount();
        if (account == null) {
            throw new HttpUnAuthorized("Người dùng chưa xác thực");
        }

        if (req.getOldPassword() == null || req.getOldPassword().trim().isEmpty()) {
            throw new HttpBadRequest("Mật khẩu cũ không được để trống");
        }

        if (req.getNewPassword() == null || req.getNewPassword().trim().isEmpty()) {
            throw new HttpBadRequest("Mật khẩu mới không được để trống");
        }

        if (req.getConfirmPassword() == null || req.getConfirmPassword().trim().isEmpty()) {
            throw new HttpBadRequest("Xác nhận mật khẩu không được để trống");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new HttpBadRequest("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (req.getOldPassword().equals(req.getNewPassword())) {
            throw new HttpBadRequest("Mật khẩu mới phải khác mật khẩu cũ");
        }

        List<RoleName> userRoles = accountRoleRepo.findRoleCodesByAccountId(account.getAccountId());
        if (userRoles.contains(RoleName.ROLE_ADMIN)) {
            throw new HttpForbiden("Tài khoản quản trị viên không thể đổi mật khẩu");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), account.getPasswordHash())) {
            throw new HttpBadRequest("Mật khẩu cũ không chính xác");
        }

        if (!account.getEmailVerified()) {
            throw new HttpForbiden("Vui lòng xác minh email trước khi đổi mật khẩu");
        }

        if (!account.getIsActive()) {
            throw new HttpForbiden("Tài khoản đã bị khóa");
        }

        account.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepo.save(account);

        activityLogRepo.save(ActivityLog.builder()
                .account(account)
                .action("CHANGE_PASSWORD")
                .details("User changed password successfully")
                .createdAt(LocalDateTime.now())
                .build());

        return ApiResponse.success(
                "Đổi mật khẩu thành công",
                "Mật khẩu của bạn đã được cập nhật");
    }

    @Override
    public ApiResponse<Object> sendResetPasswordToken(String email) {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản với email: " + email));

        String token = UUID.randomUUID().toString();
        ResetPasswordToken resetToken = new ResetPasswordToken(token, account, LocalDateTime.now().plusMinutes(15));
        resetPasswordTokenRepo.save(resetToken);

        String content = "Mã token để đổi mật khẩu của bạn là : " + token;

        emailService.sendMail(account.getEmail(), "Password Reset", content);

        activityLogRepo.save(ActivityLog.builder()
                .account(account)
                .action("RESET_PASSWORD_REQUEST")
                .details("Password reset requested for email: " + email)
                .createdAt(LocalDateTime.now())
                .build());

        return ApiResponse.success(null, "Email đặt lại mật khẩu đã được gửi!");
    }

    @Override
    public ApiResponse<Object> resetPassword(String token, String newPassword) {
        ResetPasswordToken resetToken = resetPasswordTokenRepo.findByToken(token)
                .orElseThrow(() -> new HttpBadRequest("Token không hợp lệ!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequest("Token đã hết hạn!");
        }

        Account account = resetToken.getAccount();
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepo.save(account);

        activityLogRepo.save(ActivityLog.builder()
                .account(account)
                .action("RESET_PASSWORD_SUCCESS")
                .details("Password reset successful via token")
                .createdAt(LocalDateTime.now())
                .build());

        resetPasswordTokenRepo.delete(resetToken);

        return ApiResponse.success(null, "Đặt lại mật khẩu thành công!");
    }

}
