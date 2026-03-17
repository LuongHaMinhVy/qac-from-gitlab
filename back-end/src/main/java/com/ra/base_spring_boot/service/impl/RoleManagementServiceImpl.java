package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.PermissionResponseDTO;
import com.ra.base_spring_boot.dto.resp.RoleResponseDTO;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Permission;
import com.ra.base_spring_boot.model.Role;
import com.ra.base_spring_boot.model.RolePermission;
import com.ra.base_spring_boot.repository.IPermissionRepository;
import com.ra.base_spring_boot.repository.account.IRoleRepository;
import com.ra.base_spring_boot.repository.account.RolePermissionRepository;
import com.ra.base_spring_boot.service.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementServiceImpl implements RoleManagementService {

        private final IRoleRepository roleRepository;
        private final IPermissionRepository permissionRepository;
        private final RolePermissionRepository rolePermissionRepository;

        @Override
        public ApiResponse<List<RoleResponseDTO>> getAllRoles() {
                List<RoleResponseDTO> roles = roleRepository.findAll().stream()
                                .map(this::mapToRoleResponse)
                                .collect(Collectors.toList());
                return ApiResponse.success(roles, "Lấy danh sách vai trò thành công");
        }

        @Override
        public ApiResponse<List<PermissionResponseDTO>> getAllPermissions() {
                List<PermissionResponseDTO> permissions = permissionRepository.findAll().stream()
                                .map(this::mapToPermissionResponse)
                                .collect(Collectors.toList());
                return ApiResponse.success(permissions, "Lấy danh sách quyền hạn thành công");
        }

        @Override
        public ApiResponse<Set<String>> getPermissionsByRole(Integer roleId) {
                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy vai trò"));

                Set<String> permissions = role.getRolePermissions().stream()
                                .map(rp -> rp.getPermission().getPermissionCode())
                                .collect(Collectors.toSet());

                return ApiResponse.success(permissions, "Lấy danh sách quyền hạn của vai trò thành công");
        }

        @Override
        @Transactional
        public ApiResponse<String> updateRolePermissions(Integer roleId, Set<Integer> permissionIds) {
                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy vai trò"));

                rolePermissionRepository.deleteAllByRole_RoleId(roleId);

                for (Integer pId : permissionIds) {
                        Permission permission = permissionRepository.findById(pId)
                                        .orElseThrow(() -> new HttpNotFound("Không tìm thấy quyền hạn với ID: " + pId));

                        RolePermission rolePermission = RolePermission.builder()
                                        .role(role)
                                        .permission(permission)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        rolePermissionRepository.save(rolePermission);
                }

                return ApiResponse.success(null, "Cập nhật quyền hạn của vai trò thành công");
        }

        private RoleResponseDTO mapToRoleResponse(Role role) {
                return RoleResponseDTO.builder()
                                .roleId(role.getRoleId())
                                .roleCode(role.getRoleCode())
                                .roleName(role.getRoleName())
                                .description(role.getDescription())
                                .isSystem(role.getIsSystem())
                                .createdAt(role.getCreatedAt())
                                .build();
        }

        private PermissionResponseDTO mapToPermissionResponse(Permission permission) {
                return PermissionResponseDTO.builder()
                                .permissionId(permission.getPermissionId())
                                .permissionCode(permission.getPermissionCode())
                                .permissionName(permission.getPermissionName())
                                .module(permission.getModule())
                                .description(permission.getDescription())
                                .createdAt(permission.getCreatedAt())
                                .build();
        }
}
