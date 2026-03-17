package com.ra.base_spring_boot.service.impl.permission;

import com.ra.base_spring_boot.dto.req.CheckPermissionRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.PermissionCheckResponse;
import com.ra.base_spring_boot.exception.HttpUnAuthorized;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.repository.account.AccountRoleRepo;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.account.RolePermissionRepository;
import com.ra.base_spring_boot.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

        private final IAccountRepository accountRepo;
        private final AccountRoleRepo accountRoleRepo;
        private final RolePermissionRepository rolePermissionRepo;

        @Override
        public ApiResponse<PermissionCheckResponse> checkPermission(CheckPermissionRequest request, Integer accountId) {
                if (accountId == null) {
                        throw new HttpUnAuthorized("Người dùng chưa xác thực");
                }

                Account account = accountRepo.findById(accountId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

                List<RoleName> userRoles = accountRoleRepo.findRoleCodesByAccountId(accountId);
                List<String> roleCodes = userRoles.stream()
                                .map(RoleName::name)
                                .collect(Collectors.toList());

                boolean hasPermission = userRoles.contains(RoleName.ROLE_ADMIN);

                Set<String> userPermissionsSet;
                if (hasPermission) {
                        userPermissionsSet = Set.of("ALL_PERMISSIONS");
                } else {
                        userPermissionsSet = rolePermissionRepo.findPermissionCodesByRoleCodes(userRoles);
                        hasPermission = userPermissionsSet.contains(request.getPermissionCode());
                }

                PermissionCheckResponse response = PermissionCheckResponse.builder()
                                .hasPermission(hasPermission)
                                .permissionCode(request.getPermissionCode())
                                .message(hasPermission
                                                ? "Người dùng có quyền hạn: " + request.getPermissionCode()
                                                : "Người dùng không có quyền hạn: " + request.getPermissionCode())
                                .userRoles(roleCodes.toArray(new String[0]))
                                .userPermissions(userPermissionsSet.toArray(new String[0]))
                                .build();

                return ApiResponse.success(response, "Kiểm tra quyền hạn hoàn tất");
        }

        @Override
        public ApiResponse<PermissionCheckResponse> checkRole(String roleCode, Integer accountId) {
                if (accountId == null) {
                        throw new HttpUnAuthorized("Người dùng chưa xác thực");
                }

                Account account = accountRepo.findById(accountId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

                List<RoleName> userRoles = accountRoleRepo.findRoleCodesByAccountId(accountId);
                boolean hasRole = userRoles.stream()
                                .anyMatch(role -> role.name().equals(roleCode));

                List<String> roleCodes = userRoles.stream()
                                .map(RoleName::name)
                                .collect(Collectors.toList());

                PermissionCheckResponse response = PermissionCheckResponse.builder()
                                .hasPermission(hasRole)
                                .permissionCode(roleCode)
                                .message(hasRole
                                                ? "Người dùng có vai trò: " + roleCode
                                                : "Người dùng không có vai trò: " + roleCode)
                                .userRoles(roleCodes.toArray(new String[0]))
                                .userPermissions(new String[0])
                                .build();

                return ApiResponse.success(response, "Kiểm tra vai trò hoàn tất");
        }

        @Override
        public ApiResponse<PermissionCheckResponse> checkAnyRole(String[] roleCodes, Integer accountId) {
                if (accountId == null) {
                        throw new HttpUnAuthorized("Người dùng chưa xác thực");
                }

                Account account = accountRepo.findById(accountId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

                List<RoleName> userRoles = accountRoleRepo.findRoleCodesByAccountId(accountId);
                Set<String> userRoleSet = userRoles.stream()
                                .map(RoleName::name)
                                .collect(Collectors.toSet());

                boolean hasAnyRole = false;
                for (String roleCode : roleCodes) {
                        if (userRoleSet.contains(roleCode)) {
                                hasAnyRole = true;
                                break;
                        }
                }

                List<String> userRoleList = userRoles.stream()
                                .map(RoleName::name)
                                .collect(Collectors.toList());

                PermissionCheckResponse response = PermissionCheckResponse.builder()
                                .hasPermission(hasAnyRole)
                                .permissionCode(String.join(", ", roleCodes))
                                .message(hasAnyRole
                                                ? "Người dùng có ít nhất một trong các vai trò yêu cầu"
                                                : "Người dùng không có bất kỳ vai trò nào trong số các vai trò yêu cầu")
                                .userRoles(userRoleList.toArray(new String[0]))
                                .userPermissions(new String[0])
                                .build();

                return ApiResponse.success(response, "Kiểm tra vai trò hoàn tất");
        }
}
