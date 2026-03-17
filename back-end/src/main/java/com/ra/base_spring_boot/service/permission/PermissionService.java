package com.ra.base_spring_boot.service.permission;

import com.ra.base_spring_boot.dto.req.CheckPermissionRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.PermissionCheckResponse;

public interface PermissionService {
    ApiResponse<PermissionCheckResponse> checkPermission(CheckPermissionRequest request, Integer accountId);
    ApiResponse<PermissionCheckResponse> checkRole(String roleCode, Integer accountId);
    ApiResponse<PermissionCheckResponse> checkAnyRole(String[] roleCodes, Integer accountId);
}

