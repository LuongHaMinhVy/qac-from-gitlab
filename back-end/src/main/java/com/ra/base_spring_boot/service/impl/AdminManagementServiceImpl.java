package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.AdminRequest;
import com.ra.base_spring_boot.dto.req.RoleAssignmentRequest;
import com.ra.base_spring_boot.dto.resp.AdminResponse;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.account.AccountRoleRepo;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.account.IRoleRepository;
import com.ra.base_spring_boot.repository.user.UserRepo;
import com.ra.base_spring_boot.service.AdminManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminManagementServiceImpl implements AdminManagementService {

    private final IAccountRepository accountRepository;
    private final IRoleRepository roleRepository;
    private final UserRepo userRepository;
    private final AccountRoleRepo accountRoleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<AdminResponse> createAdmin(AdminRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new HttpBadRequest("Tên đăng nhập đã tồn tại");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new HttpBadRequest("Email đã tồn tại");
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        Account savedAccount = accountRepository.save(account);

        User user = User.builder()
                .account(savedAccount)
                .fullName(request.getFullName())
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        if (request.getRoleCodes() != null) {
            for (String roleCode : request.getRoleCodes()) {
                Role role = roleRepository.findByRoleCode(RoleName.valueOf(roleCode))
                        .orElseThrow(() -> new HttpNotFound("Không tìm thấy vai trò: " + roleCode));
                AccountRole accountRole = AccountRole.builder()
                        .account(savedAccount)
                        .role(role)
                        .isPrimary(roleCode.equals(RoleName.ROLE_ADMIN.name()))
                        .assignedAt(LocalDateTime.now())
                        .build();
                accountRoleRepo.save(accountRole);
            }
        }

        return ApiResponse.success(mapToResponse(savedAccount, user), "Tạo tài khoản quân trị viên thành công");
    }

    @Override
    @Transactional
    public ApiResponse<AdminResponse> updateAdmin(Integer id, AdminRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy tài khoản với ID: " + id));

        User user = userRepository.findByAccountId(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy thông tin chi tiết người dùng"));

        if (!account.getUsername().equals(request.getUsername())
                && accountRepository.existsByUsername(request.getUsername())) {
            throw new HttpBadRequest("Tên đăng nhập đã tồn tại");
        }
        if (!account.getEmail().equals(request.getEmail()) && accountRepository.existsByEmail(request.getEmail())) {
            throw new HttpBadRequest("Email đã tồn tại");
        }

        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        user.setFullName(request.getFullName());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update roles if provided
        if (request.getRoleCodes() != null) {
            accountRoleRepo.deleteAllByAccount(account);
            for (String roleCode : request.getRoleCodes()) {
                Role role = roleRepository.findByRoleCode(RoleName.valueOf(roleCode))
                        .orElseThrow(() -> new HttpNotFound("Không tìm thấy vai trò: " + roleCode));
                AccountRole accountRole = AccountRole.builder()
                        .account(account)
                        .role(role)
                        .isPrimary(roleCode.equals(RoleName.ROLE_ADMIN.name()))
                        .assignedAt(LocalDateTime.now())
                        .build();
                accountRoleRepo.save(accountRole);
            }
        }

        return ApiResponse.success(mapToResponse(account, user), "Cập nhật tài khoản quản trị viên thành công");
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteAdmin(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy tài khoản với ID: " + id));

        account.setIsActive(false);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        return ApiResponse.success("Vô hiệu hóa tài khoản thành công",
                "Tài khoản đã được chuyển sang trạng thái không hoạt động");
    }

    @Override
    public ApiResponse<AdminResponse> getAdminById(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy tài khoản với ID: " + id));
        User user = userRepository.findByAccountId(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy thông tin chi tiết người dùng"));

        return ApiResponse.success(mapToResponse(account, user), "Lấy thông tin tài khoản thành công");
    }

    @Override
    public ApiResponse<List<AdminResponse>> getAllEditors(String search, Pageable pageable) {
        Page<Account> accounts = accountRepository.findAllEditorsWithFilter(search, pageable);

        List<AdminResponse> responses = accounts.getContent().stream()
                .map(acc -> {
                    User user = userRepository.findByAccountId(acc.getAccountId()).orElse(null);
                    return mapToResponse(acc, user);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(responses, "Lấy danh sách biên tập viên thành công");
    }

    @Override
    @Transactional
    public ApiResponse<AdminResponse> assignRoles(Integer id, RoleAssignmentRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy tài khoản với ID: " + id));

        accountRoleRepo.deleteAllByAccount(account);
        for (String roleCode : request.getRoleCodes()) {
            Role role = roleRepository.findByRoleCode(RoleName.valueOf(roleCode))
                    .orElseThrow(() -> new HttpNotFound("Không tìm thấy vai trò: " + roleCode));
            AccountRole accountRole = AccountRole.builder()
                    .account(account)
                    .role(role)
                    .isPrimary(false)
                    .assignedAt(LocalDateTime.now())
                    .build();
            accountRoleRepo.save(accountRole);
        }

        User user = userRepository.findByAccountId(id).orElse(null);
        return ApiResponse.success(mapToResponse(account, user), "Gán vai trò thành công");
    }

    private AdminResponse mapToResponse(Account account, User user) {
        Set<String> roles = account.getAccountRoles().stream()
                .map(ar -> ar.getRole().getRoleCode().name())
                .collect(Collectors.toSet());

        Set<String> permissions = account.getAccountRoles().stream()
                .flatMap(ar -> ar.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getPermissionCode())
                .collect(Collectors.toSet());

        return AdminResponse.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(user != null ? user.getFullName() : null)
                .isActive(account.getIsActive())
                .roles(roles)
                .permissions(permissions)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
