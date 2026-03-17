package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.DashboardChartResponse;
import com.ra.base_spring_boot.dto.resp.DashboardSummaryResponse;
import com.ra.base_spring_boot.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Báo cáo thống kê hệ thống")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('" + PermissionCode.ANALYTICS_VIEW + "')")
    @Operation(summary = "Lấy báo cáo tổng quan (Admin)", description = "Trả về số lượng user, bài viết, lượt xem, bình luận...")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/charts")
    @PreAuthorize("hasAuthority('" + PermissionCode.ANALYTICS_VIEW + "')")
    public ResponseEntity<ApiResponse<DashboardChartResponse>> getChartData() {
        return ResponseEntity.ok(dashboardService.getChartData());
    }
}
