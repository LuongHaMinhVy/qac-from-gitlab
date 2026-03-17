package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ApiLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.service.ApiLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/api-logs")
@RequiredArgsConstructor
@Tag(name = "API Logging", description = "Quản lý và tra cứu logs API")
public class ApiLogController {

    private final ApiLogService apiLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.API_LOG_VIEW + "')")
    @Operation(summary = "Lấy danh sách API logs", description = "Chỉ dành cho ADMIN. Hỗ trợ lọc theo nhiều tiêu chí.")
    public ResponseEntity<ApiResponse<List<ApiLogResponseDTO>>> getApiLogs(
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity
                .ok(apiLogService.getLogs(accountId, method, statusCode, endpoint, startDate, endDate, pageable));
    }
}
