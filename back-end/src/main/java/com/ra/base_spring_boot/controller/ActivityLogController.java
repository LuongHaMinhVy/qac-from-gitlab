package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.service.ActivitiLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/activity-logs")
@RequiredArgsConstructor
@Tag(name = "Activity Logging", description = "Quản lý và tra cứu lịch sử hoạt động của người dùng")
public class ActivityLogController {

    private final ActivitiLogService activityLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.ACTIVITY_LOG_VIEW + "')")
    @Operation(summary = "Lấy danh sách Activity logs", description = "Chỉ dành cho ADMIN. Xem lịch sử hoạt động của users.")
    public ResponseEntity<ApiResponse<List<ActivityLogResponseDTO>>> getActivityLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword) {

        return ResponseEntity.ok(activityLogService.getLogs(page, size, sortBy, sortDir, accountId, action, keyword));
    }
}
