package com.ra.base_spring_boot.service.impl.adminmember;

import com.ra.base_spring_boot.dto.req.ApproveAuthorRoleRequest;
import com.ra.base_spring_boot.dto.req.MemberSearchRequest;
import com.ra.base_spring_boot.dto.resp.*;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.exception.HttpUnAuthorized;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.ApprovalStatus;
import com.ra.base_spring_boot.model.constants.Gender;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.account.AccountRoleRepo;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.account.IRoleRepository;
import com.ra.base_spring_boot.repository.role.RoleRequestRepo;
import com.ra.base_spring_boot.repository.user.UserRepo;
import com.ra.base_spring_boot.service.adminmember.AdminMembersService;
import com.ra.base_spring_boot.service.impl.auth.EmailService;
import com.ra.base_spring_boot.service.MediaService;
import com.ra.base_spring_boot.utils.ExcelService;
import com.ra.base_spring_boot.utils.OtpUtil;
import com.ra.base_spring_boot.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberServiceImpl implements AdminMembersService {
    private final ExcelService excelService;
    private final AccountRoleRepo accountRoleRepo;
    private final UserRepo userRepo;
    private final IAccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final IRoleRepository roleRepo;
    private final EmailService emailService;
    private final MediaService mediaService;
    private final RoleRequestRepo roleRequestRepo;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public ApiResponse<List<RoleRequestResponse>> getPendingAuthorRequests() {
        List<RoleRequest> requests = roleRequestRepo.findByStatus(ApprovalStatus.pending);
        List<RoleRequestResponse> response = requests.stream()
                .map(req -> RoleRequestResponse.builder()
                        .id(req.getId())
                        .accountId(req.getAccount().getAccountId())
                        .accountEmail(req.getAccount().getEmail())
                        .accountUsername(req.getAccount().getUsername())
                        .requestedRoleCode(req.getRequestedRole().getRoleCode().name())
                        .requestedRoleName(req.getRequestedRole().getRoleName())
                        .status(req.getStatus())
                        .reason(req.getReason())
                        .createdAt(req.getCreatedAt())
                        .build())
                .toList();
        return ApiResponse.success(response, "Get pending author requests successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> approveAuthorRole(ApproveAuthorRoleRequest request) {
        Account currentAccount = SecurityUtils.getCurrentAccount();
        if (currentAccount == null) {
            throw new HttpUnAuthorized("User not authenticated");
        }

        RoleRequest roleRequest = roleRequestRepo.findById(request.getRoleRequestId())
                .orElseThrow(() -> new HttpNotFound("Role request not found with id: " + request.getRoleRequestId()));

        if (roleRequest.getStatus() != ApprovalStatus.pending) {
            throw new HttpBadRequest("Request is already " + roleRequest.getStatus());
        }

        roleRequest.setStatus(ApprovalStatus.approved);
        roleRequest.setReviewedBy(currentAccount);
        roleRequest.setReviewedAt(LocalDateTime.now());
        roleRequest.setReviewComments(request.getReviewComments());
        roleRequestRepo.save(roleRequest);

        Account targetAccount = roleRequest.getAccount();
        Role authorRole = roleRequest.getRequestedRole();

        boolean hasRole = accountRoleRepo.findRoleCodesByAccountId(targetAccount.getAccountId())
                .contains(authorRole.getRoleCode());

        if (!hasRole) {
            AccountRole accountRole = AccountRole.builder()
                    .account(targetAccount)
                    .role(authorRole)
                    .isPrimary(false) // Keep existing primary role
                    .assignedBy(currentAccount)
                    .assignedAt(LocalDateTime.now())
                    .build();
            accountRoleRepo.save(accountRole);
        }

        try {
            String content = "<html><body>" +
                    "<h2>Congratulations!</h2>" +
                    "<p>Your request to become an <strong>Author</strong> has been approved.</p>" +
                    "<p>You can now start writing and submitting articles for review.</p>" +
                    "</body></html>";
            emailService.sendMail(targetAccount.getEmail(), "Author Role Approved", content);
        } catch (Exception e) {
        }

        return ApiResponse.success("Approved",
                "Author role approved successfully for user: " + targetAccount.getUsername());
    }

    @Override
    @Transactional
    public ApiResponse<String> rejectAuthorRole(ApproveAuthorRoleRequest request) {
        Account currentAccount = SecurityUtils.getCurrentAccount();
        if (currentAccount == null) {
            throw new HttpUnAuthorized("User not authenticated");
        }

        RoleRequest roleRequest = roleRequestRepo.findById(request.getRoleRequestId())
                .orElseThrow(() -> new HttpNotFound("Role request not found with id: " + request.getRoleRequestId()));

        if (roleRequest.getStatus() != ApprovalStatus.pending) {
            throw new HttpBadRequest("Request is already " + roleRequest.getStatus());
        }

        roleRequest.setStatus(ApprovalStatus.rejected);
        roleRequest.setReviewedBy(currentAccount);
        roleRequest.setReviewedAt(LocalDateTime.now());
        roleRequest.setReviewComments(request.getReviewComments());
        roleRequestRepo.save(roleRequest);

        try {
            String content = "<html><body>" +
                    "<h2>Role Request Update</h2>" +
                    "<p>Your request to become an Author has been declined.</p>" +
                    "<p><strong>Reason:</strong> "
                    + (request.getReviewComments() != null ? request.getReviewComments() : "No reason provided")
                    + "</p>" +
                    "</body></html>";
            emailService.sendMail(roleRequest.getAccount().getEmail(), "Author Role Request Update", content);
        } catch (Exception e) {
        }

        return ApiResponse.success("Rejected", "Author role rejected successfully");
    }

    @Override
    @Transactional
    public ApiResponse<MemberImportResponse> importMembersFromExcel(MultipartFile file) {
        Account currentAccount = SecurityUtils.getCurrentAccount();
        if (currentAccount == null) {
            throw new HttpUnAuthorized("User not authenticated");
        }

        List<RoleName> currentUserRoles = accountRoleRepo.findRoleCodesByAccountId(currentAccount.getAccountId());
        if (!currentUserRoles.contains(RoleName.ROLE_ADMIN)) {
            throw new HttpForbiden("Only admin can import members");
        }

        if (file == null || file.isEmpty()) {
            throw new HttpBadRequest("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new HttpBadRequest("File name cannot be null");
        }

        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            throw new HttpBadRequest("File must be an Excel file (.xlsx or .xls)");
        }

        List<ExcelService.MemberImportData> members;
        try {
            members = excelService.importMembersFromExcel(file);
        } catch (IOException e) {
            throw new HttpBadRequest("Failed to read Excel file: " + e.getMessage());
        }

        if (members.isEmpty()) {
            throw new HttpBadRequest("Excel file is empty or has no valid data");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < members.size(); i++) {
            ExcelService.MemberImportData memberData = members.get(i);
            int rowNumber = i + 2;

            try {
                validateMemberImportData(memberData, rowNumber);

                if (accountRepo.existsByEmail(memberData.getEmail().trim())) {
                    errors.add("Row " + rowNumber + ": Email already exists: " + memberData.getEmail());
                    failCount++;
                    continue;
                }

                if (accountRepo.existsByUsername(memberData.getUsername().trim())) {
                    errors.add("Row " + rowNumber + ": Username already exists: " + memberData.getUsername());
                    failCount++;
                    continue;
                }

                String randomPassword = OtpUtil.generatePassword();
                String encodedPassword = passwordEncoder.encode(randomPassword);

                Account newAccount = Account.builder()
                        .email(memberData.getEmail().trim())
                        .username(memberData.getUsername().trim())
                        .passwordHash(encodedPassword)
                        .isActive(true)
                        .emailVerified(true)
                        .build();

                Account savedAccount = accountRepo.save(newAccount);

                try {
                    String loginUrl = frontendUrl + "/login";
                    String emailContent = "<html><body>" +
                            "<p>Your account has been created.</p>" +
                            "<p><strong>Username:</strong> " + savedAccount.getUsername() + "</p>" +
                            "<p><strong>Email:</strong> " + savedAccount.getEmail() + "</p>" +
                            "<p><strong>Password:</strong> " + randomPassword + "</p>" +
                            "<p>Please click the link below to login:</p>" +
                            "<p><a href=\"" + loginUrl + "\">" + loginUrl + "</a></p>" +
                            "<p>Please change your password after first login.</p>" +
                            "</body></html>";
                    emailService.sendMail(savedAccount.getEmail(), "Account Created - Password Information",
                            emailContent);
                } catch (Exception e) {
                    errors.add("Row " + rowNumber + ": Account created but failed to send password email: "
                            + e.getMessage());
                }

                User newUser = User.builder()
                        .account(savedAccount)
                        .fullName(memberData.getFullName().trim())
                        .phone(memberData.getPhone() != null && !memberData.getPhone().trim().isEmpty()
                                ? memberData.getPhone().trim()
                                : null)
                        .gender(memberData.getGender())
                        .dateOfBirth(memberData.getDateOfBirth())
                        .address(memberData.getAddress() != null && !memberData.getAddress().trim().isEmpty()
                                ? memberData.getAddress().trim()
                                : null)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                userRepo.save(newUser);

                Role memberRole = roleRepo.findByRoleCode(RoleName.ROLE_USER)
                        .orElseThrow(() -> new HttpBadRequest("Member role (ROLE_USER) not found"));

                AccountRole accountRole = AccountRole.builder()
                        .account(savedAccount)
                        .role(memberRole)
                        .isPrimary(true)
                        .assignedBy(currentAccount)
                        .assignedAt(LocalDateTime.now())
                        .build();

                accountRoleRepo.save(accountRole);

                successCount++;
            } catch (Exception e) {
                errors.add("Row " + rowNumber + ": " + e.getMessage());
                failCount++;
            }
        }

        String message = String.format("Import completed. Success: %d, Failed: %d", successCount, failCount);
        if (!errors.isEmpty() && errors.size() <= 20) {
            message += ". Errors: " + String.join("; ", errors);
        } else if (!errors.isEmpty()) {
            message += ". First 20 errors: " + String.join("; ", errors.subList(0, 20));
        }

        MemberImportResponse response = MemberImportResponse.builder()
                .totalRows(members.size())
                .successCount(successCount)
                .failCount(failCount)
                .message(message)
                .build();

        return ApiResponse.success(response, "Members import completed");
    }

    private void validateMemberImportData(ExcelService.MemberImportData memberData, int rowNumber) {
        if (memberData.getEmail() == null || memberData.getEmail().trim().isEmpty()) {
            throw new HttpBadRequest("Row " + rowNumber + ": Email is required");
        }

        String email = memberData.getEmail().trim();
        if (email.length() > 100) {
            throw new HttpBadRequest("Row " + rowNumber + ": Email must not exceed 100 characters");
        }

        String emailRegex = "^[A-Za-z][A-Za-z0-9]{4,}@gmail\\.com$";
        if (!email.matches(emailRegex)) {
            throw new HttpBadRequest("Row " + rowNumber
                    + ": Email must be a valid Gmail address (format: username@gmail.com, username must start with letter and have at least 5 characters)");
        }

        if (memberData.getUsername() == null || memberData.getUsername().trim().isEmpty()) {
            throw new HttpBadRequest("Row " + rowNumber + ": Username is required");
        }

        String username = memberData.getUsername().trim();
        if (username.length() > 50) {
            throw new HttpBadRequest("Row " + rowNumber + ": Username must not exceed 50 characters");
        }

        if (memberData.getFullName() == null || memberData.getFullName().trim().isEmpty()) {
            throw new HttpBadRequest("Row " + rowNumber + ": Full name is required");
        }

        String fullName = memberData.getFullName().trim();
        if (fullName.length() > 100) {
            throw new HttpBadRequest("Row " + rowNumber + ": Full name must not exceed 100 characters");
        }

        if (memberData.getPhone() != null && !memberData.getPhone().trim().isEmpty()) {
            String phone = memberData.getPhone().trim();
            if (phone.length() > 20) {
                throw new HttpBadRequest("Row " + rowNumber + ": Phone must not exceed 20 characters");
            }

            if (!phone.matches("^[0-9+]+$")) {
                throw new HttpBadRequest("Row " + rowNumber + ": Phone must contain only numbers and + sign");
            }

            boolean isValid = false;
            if (phone.startsWith("0") && phone.length() == 10 && phone.matches("^0[3|5|7|8|9][0-9]{8}$")) {
                isValid = true;
            } else if (phone.startsWith("+84") && phone.length() == 12 && phone.matches("^\\+84[3|5|7|8|9][0-9]{8}$")) {
                isValid = true;
            } else if (phone.startsWith("84") && phone.length() == 11 && phone.matches("^84[3|5|7|8|9][0-9]{8}$")) {
                isValid = true;
            }

            if (!isValid) {
                throw new HttpBadRequest("Row " + rowNumber
                        + ": Phone must be a valid Vietnamese phone number (format: 0xxx, +84xxx, or 84xxx)");
            }
        }

        if (memberData.getGender() != null) {
            try {
                Gender.valueOf(memberData.getGender().name());
            } catch (IllegalArgumentException e) {
                throw new HttpBadRequest("Row " + rowNumber + ": Gender must be one of: MALE, FEMALE, OTHER");
            }
        }

        if (memberData.getDateOfBirth() != null) {
            LocalDate dob = memberData.getDateOfBirth();
            if (dob.isAfter(LocalDate.now())) {
                throw new HttpBadRequest("Row " + rowNumber + ": Date of birth cannot be in the future");
            }
        }

        if (memberData.getAddress() != null && !memberData.getAddress().trim().isEmpty()) {
            String address = memberData.getAddress().trim();
            if (address.length() > 1000) {
                throw new HttpBadRequest("Row " + rowNumber + ": Address must not exceed 1000 characters");
            }
        }
    }

    @Override
    public ApiResponse<AdminMemberResponse> getMemberByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new HttpNotFound("Member not found"));
        AdminMemberResponse data = AdminMemberResponse.builder()
                .userId(userId)
                .accountId(user.getAccount().getAccountId())
                .username(user.getAccount().getUsername())
                .email(user.getAccount().getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar() != null ? mediaService.convertToDTO(user.getAvatar()) : null)
                .isActive(user.getAccount().getIsActive())
                .emailVerified(user.getAccount().getEmailVerified())
                .roles(
                        user.getAccount().getRoles()
                                .stream()
                                .map(r -> r.getRoleCode().name())
                                .toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        return ApiResponse.success(
                data, "Get member by user id successfully");
    }

    @Override
    public ApiResponse<AdminMemberResponse> updateMemberStatus(Long userId, boolean isActive) {
        Account currentAccount = SecurityUtils.getCurrentAccount();
        if (currentAccount == null) {
            throw new HttpUnAuthorized("User not authenticated");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new HttpNotFound("User not found with id: " + userId));
        Account account = user.getAccount();
        if (account.getRoles().stream()
                .anyMatch(r -> r.getRoleCode() == RoleName.ROLE_ADMIN)) {
            throw new HttpBadRequest("Không thể cập nhật trạng thái của Admin");
        }
        if (account.getIsActive() == isActive) {
            throw new HttpBadRequest(
                    "Trạng thái người dùng không thay đổi. Trạng thái hiện tại: " + (isActive ? "Hoạt động" : "Bị khóa")
                            + ", trạng thái mới: " + (isActive ? "Bị khóa" : "Hoạt động"));
        }
        account.setIsActive(isActive);
        accountRepo.save(account);
        AdminMemberResponse data = AdminMemberResponse.builder()
                .accountId(account.getAccountId())
                .userId(userId)
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar() != null ? mediaService.convertToDTO(user.getAvatar()) : null)
                .emailVerified(account.getEmailVerified())
                .roles(
                        user.getAccount().getRoles()
                                .stream()
                                .map(r -> r.getRoleCode().name())
                                .toList())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .isActive(account.getIsActive())
                .build();
        return ApiResponse.success(
                data, account.getIsActive() ? "Mở khóa thành viên thành công" : "Khóa thành viên thành công");
    }

    @Override
    public ApiResponse<List<AdminMemberResponse>> getMembersWithPagination(MemberSearchRequest request) {
        Account currentAccount = SecurityUtils.getCurrentAccount();
        if (currentAccount == null) {
            throw new HttpUnAuthorized("User not authenticated");
        }

        String sortField = (request.getSort() != null && !request.getSort().isBlank())
                ? request.getSort()
                : "createdAt";
        String direction = (request.getDirection() != null && !request.getDirection().isBlank())
                ? request.getDirection()
                : "desc";

        Sort sort = Sort.by(
                Sort.Direction.fromString(direction),
                sortField);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort);

        RoleName roleName = null;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            roleName = RoleName.valueOf(request.getRole().toUpperCase());
        }

        Page<User> pageData = userRepo.searchMembersExcludeAdmin(
                request.getEmail(),
                request.getIsActive(),
                roleName,
                pageable);

        List<AdminMemberResponse> result = pageData.getContent()
                .stream()
                .map(user -> {
                    Account account = user.getAccount();

                    return AdminMemberResponse.builder()
                            .userId(user.getId())
                            .accountId(account.getAccountId())
                            .username(account.getUsername())
                            .email(account.getEmail())
                            .fullName(user.getFullName())
                            .phone(user.getPhone())
                            .avatar(user.getAvatar() != null ? mediaService.convertToDTO(user.getAvatar()) : null)
                            .isActive(account.getIsActive())
                            .emailVerified(account.getEmailVerified())
                            .roles(account.getRoles().stream().map(r -> r.getRoleCode().name()).toList())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .build();
                })
                .toList();

        Pagination pagination = Pagination.builder()
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
        return ApiResponse.<List<AdminMemberResponse>>builder()
                .success(true)
                .message("Get members successfully")
                .data(result)
                .errors(null)
                .pagination(pagination)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
