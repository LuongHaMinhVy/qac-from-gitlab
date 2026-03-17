package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.BulkSystemSettingRequest;
import com.ra.base_spring_boot.dto.req.SystemSettingRequest;
import com.ra.base_spring_boot.dto.req.SystemSettingUpdateRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.SystemSettingResponse;
import com.ra.base_spring_boot.model.constants.SettingCategory;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "System Settings", description = "Quản lý thông tin tĩnh toàn trang (cấu hình website, liên hệ, SEO, mạng xã hội...)")
public class SystemSettingController {

    private final SystemSettingService settingService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Tạo cài đặt mới", description = "Chỉ dành cho ADMIN. Tạo một cài đặt mới với key duy nhất.")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> createSetting(
            @Valid @RequestBody SystemSettingRequest request) {
        return new ResponseEntity<>(settingService.createSetting(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Cập nhật cài đặt theo ID", description = "Chỉ dành cho ADMIN. Cập nhật value, description, isPublic.")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
            @PathVariable Long id,
            @Valid @RequestBody SystemSettingUpdateRequest request) {
        return ResponseEntity.ok(settingService.updateSetting(id, request));
    }

    @PutMapping("/key/{key}")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Cập nhật cài đặt theo key", description = "Chỉ dành cho ADMIN. Cập nhật setting theo key thay vì ID.")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSettingByKey(
            @PathVariable String key,
            @Valid @RequestBody SystemSettingUpdateRequest request) {
        return ResponseEntity.ok(settingService.updateSettingByKey(key, request));
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Cập nhật hàng loạt", description = "Chỉ dành cho ADMIN. Cập nhật nhiều settings cùng lúc.")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> bulkUpdateSettings(
            @Valid @RequestBody BulkSystemSettingRequest request) {
        return ResponseEntity.ok(settingService.bulkUpdateSettings(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Xóa cài đặt", description = "Chỉ dành cho ADMIN. Không thể xóa các cài đặt quan trọng (site_name, site_logo, contact_email).")
    public ResponseEntity<ApiResponse<String>> deleteSetting(@PathVariable Long id) {
        return ResponseEntity.ok(settingService.deleteSetting(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Lấy tất cả cài đặt (Admin)", description = "Chỉ dành cho ADMIN. Hỗ trợ phân trang và tìm kiếm.")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAllSettings(
            @Parameter(description = "Từ khóa tìm kiếm (key, value, description)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo danh mục") @RequestParam(required = false) SettingCategory category,
            @PageableDefault(size = 20, sort = "settingKey", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(settingService.getAllSettings(keyword, category, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Lấy cài đặt theo ID", description = "Chỉ dành cho ADMIN.")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> getSettingById(@PathVariable Long id) {
        return ResponseEntity.ok(settingService.getSettingById(id));
    }

    @GetMapping("/key/{key}")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Lấy cài đặt theo key", description = "Chỉ dành cho ADMIN.")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(settingService.getSettingByKey(key));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('" + PermissionCode.SYSTEM_SETTING + "')")
    @Operation(summary = "Lấy cài đặt theo danh mục (Admin)", description = "Chỉ dành cho ADMIN. Lấy tất cả settings trong một category.")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getSettingsByCategory(
            @PathVariable SettingCategory category) {
        return ResponseEntity.ok(settingService.getSettingsByCategory(category));
    }

    @GetMapping("/public")
    @Operation(summary = "Lấy tất cả cài đặt public", description = "API công khai. Lấy các settings có isPublic=true cho client sử dụng.")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getPublicSettings() {
        return ResponseEntity.ok(settingService.getPublicSettings());
    }

    @GetMapping("/public/category/{category}")
    @Operation(summary = "Lấy cài đặt public theo danh mục", description = "API công khai. Lấy settings public theo category.")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getPublicSettingsByCategory(
            @PathVariable SettingCategory category) {
        return ResponseEntity.ok(settingService.getPublicSettingsByCategory(category));
    }
}
