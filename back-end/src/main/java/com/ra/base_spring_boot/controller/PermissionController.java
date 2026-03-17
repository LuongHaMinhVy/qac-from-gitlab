package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.CheckPermissionRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.PermissionCheckResponse;
import com.ra.base_spring_boot.service.permission.PermissionService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<PermissionCheckResponse>> checkPermission(
            @Valid @RequestBody CheckPermissionRequest request) {

        Integer accountId = SecurityUtils.getCurrentAccountId();

        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("User not authenticated", null));
        }

        ApiResponse<PermissionCheckResponse> response = permissionService.checkPermission(request, accountId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-role/{roleCode}")
    public ResponseEntity<ApiResponse<PermissionCheckResponse>> checkRole(
            @PathVariable String roleCode) {

        Integer accountId = SecurityUtils.getCurrentAccountId();

        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("User not authenticated", null));
        }

        ApiResponse<PermissionCheckResponse> response = permissionService.checkRole(roleCode, accountId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-any-role")
    public ResponseEntity<ApiResponse<PermissionCheckResponse>> checkAnyRole(
            @RequestBody String[] roleCodes) {

        Integer accountId = SecurityUtils.getCurrentAccountId();

        if (accountId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("User not authenticated", null));
        }

        ApiResponse<PermissionCheckResponse> response = permissionService.checkAnyRole(roleCodes, accountId);
        return ResponseEntity.ok(response);
    }
}
