package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.PermissionResponseDTO;
import com.ra.base_spring_boot.dto.resp.RoleResponseDTO;
import com.ra.base_spring_boot.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Quản lý vai trò (Role) và phân quyền (Permission) động.")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các vai trò")
    public ResponseEntity<ApiResponse<List<RoleResponseDTO>>> getAllRoles() {
        return ResponseEntity.ok(roleManagementService.getAllRoles());
    }

    @GetMapping("/permissions")
    @Operation(summary = "Lấy danh sách tất cả các quyền hạn trong hệ thống")
    public ResponseEntity<ApiResponse<List<PermissionResponseDTO>>> getAllPermissions() {
        return ResponseEntity.ok(roleManagementService.getAllPermissions());
    }

    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Lấy danh sách mã quyền hạn của một vai trò cụ thể")
    public ResponseEntity<ApiResponse<Set<String>>> getPermissionsByRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(roleManagementService.getPermissionsByRole(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('" + PermissionCode.PERMISSION_MANAGE + "')")
    @Operation(summary = "Cập nhật danh sách quyền hạn cho một vai trò")
    public ResponseEntity<ApiResponse<String>> updateRolePermissions(
            @PathVariable Integer roleId,
            @RequestBody Set<Integer> permissionIds) {
        return ResponseEntity.ok(roleManagementService.updateRolePermissions(roleId, permissionIds));
    }
}
