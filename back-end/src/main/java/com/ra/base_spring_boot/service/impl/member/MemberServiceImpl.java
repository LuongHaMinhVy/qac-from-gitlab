package com.ra.base_spring_boot.service.impl.member;

import com.ra.base_spring_boot.dto.req.AuthorRoleRequest;
import com.ra.base_spring_boot.dto.req.MemberRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MemberResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpUnAuthorized;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.ApprovalStatus;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.account.AccountRoleRepo;
import com.ra.base_spring_boot.repository.account.IRoleRepository;
import com.ra.base_spring_boot.repository.role.RoleRequestRepo;
import com.ra.base_spring_boot.repository.user.UserRepo;
import com.ra.base_spring_boot.service.member.MemberService;
import com.ra.base_spring_boot.service.MediaService;
import com.ra.base_spring_boot.service.ContentModerationService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserRepo userRepo;
    private final IAccountRepository accountRepo;
    private final AccountRoleRepo accountRoleRepo;
    private final IRoleRepository roleRepo;
    private final RoleRequestRepo roleRequestRepo;
    private final MediaService mediaService;
    private final ContentModerationService contentModerationService;

    @Override
    @Transactional
    public ApiResponse<MemberResponse> getCurrentMember() {
        Account account = SecurityUtils.getCurrentAccount();
        if (account == null) {
            throw new HttpUnAuthorized("Người dùng chưa xác thực");
        }
        Account accountWithUser = accountRepo.findById(account.getAccountId())
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy tài khoản"));

        User user = userRepo.findByAccountId(account.getAccountId())
                .orElse(null);

        if (user == null) {
            user = User.builder()
                    .account(accountWithUser)
                    .fullName(accountWithUser.getUsername())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            user = userRepo.save(user);
        }

        MemberResponse response = MemberResponse.builder()
                .userId(user.getId())
                .accountId(account.getAccountId())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar() != null ? mediaService.convertToDTO(user.getAvatar()) : null)
                .bio(user.getBio())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        return ApiResponse.success(response, "Lấy thông tin thành viên thành công");
    }

    @Override
    @Transactional
    public ApiResponse<MemberResponse> updateMember(MemberRequest request,
            MultipartFile file) {
        Account account = SecurityUtils.getCurrentAccount();
        if (account == null) {
            throw new HttpUnAuthorized("Người dùng chưa xác thực");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new HttpBadRequest("Họ tên không được để trống");
        }

        if (request.getFullName().length() > 100) {
            throw new HttpBadRequest("Họ tên không được vượt quá 100 ký tự");
        }

        if (request.getPhone() != null && request.getPhone().length() > 20) {
            throw new HttpBadRequest("Số điện thoại không được vượt quá 20 ký tự");
        }

        contentModerationService.validateContent(request.getFullName());
        if (request.getBio() != null) {
            contentModerationService.validateContent(request.getBio());
        }
        if (request.getAddress() != null) {
            contentModerationService.validateContent(request.getAddress());
        }

        if (!accountRepo.existsById(account.getAccountId())) {
            throw new HttpBadRequest("Không tìm thấy tài khoản");
        }

        User existingUser = userRepo.findByAccountId(account.getAccountId())
                .orElse(null);

        if (existingUser == null) {
            throw new HttpBadRequest("Không tìm thấy hồ sơ thành viên. Vui lòng tạo hồ sơ trước.");
        }

        existingUser.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) {
            existingUser.setPhone(request.getPhone().trim());
        }
        if (file != null && !file.isEmpty()) {
            existingUser.setAvatar(mediaService.uploadFile(file));
        } else if (request.getMediaId() != null) {
            existingUser.setAvatar(mediaService.findById(request.getMediaId()));
        }
        if (request.getBio() != null) {
            existingUser.setBio(request.getBio().trim());
        }
        existingUser.setDateOfBirth(request.getDateOfBirth());
        existingUser.setGender(request.getGender());
        if (request.getAddress() != null) {
            existingUser.setAddress(request.getAddress().trim());
        }
        existingUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepo.save(existingUser);

        MemberResponse response = MemberResponse.builder()
                .userId(savedUser.getId())
                .accountId(account.getAccountId())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .avatar(savedUser.getAvatar() != null ? mediaService.convertToDTO(savedUser.getAvatar()) : null)
                .bio(savedUser.getBio())
                .dateOfBirth(savedUser.getDateOfBirth())
                .gender(savedUser.getGender())
                .address(savedUser.getAddress())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        return ApiResponse.success(response, "Cập nhật thông tin thành viên thành công");
    }

    @Override
    @Transactional
    public ApiResponse<String> requestAuthorRole(AuthorRoleRequest request) {
        Account account = SecurityUtils.getCurrentAccount();
        if (account == null) {
            throw new HttpUnAuthorized("Người dùng chưa xác thực");
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new HttpBadRequest("Lý do không được để trống");
        }

        contentModerationService.validateContent(request.getReason());

        List<RoleName> userRoles = accountRoleRepo.findRoleCodesByAccountId(account.getAccountId());
        if (userRoles.contains(RoleName.ROLE_AUTHOR)) {
            throw new HttpBadRequest("Bạn đã có vai trò tác giả");
        }

        if (userRoles.contains(RoleName.ROLE_ADMIN)) {
            throw new HttpForbiden("Quản trị viên không thể yêu cầu vai trò tác giả");
        }

        Role authorRole = roleRepo.findByRoleCode(RoleName.ROLE_AUTHOR)
                .orElseThrow(() -> new HttpBadRequest("Không tìm thấy vai trò tác giả"));

        RoleRequest existingRequest = roleRequestRepo
                .findByAccount_AccountIdAndRequestedRole_RoleCodeAndStatus(
                        account.getAccountId(),
                        RoleName.ROLE_AUTHOR,
                        ApprovalStatus.pending)
                .orElse(null);

        if (existingRequest != null) {
            throw new HttpBadRequest("Bạn đã có một yêu cầu vai trò tác giả đang chờ duyệt");
        }

        RoleRequest roleRequest = RoleRequest.builder()
                .account(account)
                .requestedRole(authorRole)
                .status(ApprovalStatus.pending)
                .reason(request.getReason().trim())
                .createdAt(LocalDateTime.now())
                .build();

        roleRequestRepo.save(roleRequest);

        return ApiResponse.success(
                "Gửi yêu cầu vai trò tác giả thành công",
                "Yêu cầu của bạn đang chờ phê duyệt");
    }
}
