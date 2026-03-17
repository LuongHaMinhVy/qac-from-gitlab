package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.MenuRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MenuResponse;
import com.ra.base_spring_boot.model.constants.MenuLocation;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "Menu Management", description = "Quản lý hệ thống menu và điều hướng đa tầng.")
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.MENU_MANAGE + "')")
    @Operation(summary = "Tạo menu mới", description = "Chỉ dành cho ADMIN.")
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(@Valid @RequestBody MenuRequest request) {
        return new ResponseEntity<>(menuService.createMenu(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.MENU_MANAGE + "')")
    @Operation(summary = "Cập nhật menu", description = "Chỉ dành cho ADMIN.")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(@PathVariable Long id,
            @Valid @RequestBody MenuRequest request) {
        return ResponseEntity.ok(menuService.updateMenu(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.MENU_MANAGE + "')")
    @Operation(summary = "Xóa menu", description = "Chỉ dành cho ADMIN.")
    public ResponseEntity<ApiResponse<String>> deleteMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.deleteMenu(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin menu theo ID")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getMenuById(id));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả menu")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllMenus() {
        return ResponseEntity.ok(menuService.getAllMenus());
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Lấy danh sách menu theo vị trí (Header/Footer/Sidebar)")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByLocation(@PathVariable MenuLocation location) {
        return ResponseEntity.ok(menuService.getMenusByLocation(location));
    }
}
