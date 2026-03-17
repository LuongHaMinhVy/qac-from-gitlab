package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.PermissionResponseDTO;
import com.ra.base_spring_boot.dto.resp.RoleResponseDTO;

import java.util.List;
import java.util.Set;

public interface RoleManagementService {
    ApiResponse<List<RoleResponseDTO>> getAllRoles();

    ApiResponse<List<PermissionResponseDTO>> getAllPermissions();

    ApiResponse<Set<String>> getPermissionsByRole(Integer roleId);

    ApiResponse<String> updateRolePermissions(Integer roleId, Set<Integer> permissionIds);
}
