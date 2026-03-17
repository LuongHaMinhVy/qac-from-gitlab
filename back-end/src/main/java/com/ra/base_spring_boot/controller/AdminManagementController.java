package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.AdminRequest;
import com.ra.base_spring_boot.dto.req.RoleAssignmentRequest;
import com.ra.base_spring_boot.dto.resp.AdminResponse;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.service.AdminManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/management")
@RequiredArgsConstructor
@Tag(name = "Staff Management", description = "Quản lý tài khoản Biên tập viên (Editor).")
@PreAuthorize("hasAnyAuthority('USER_VIEW', 'USER_UPDATE', 'ROLE_ASSIGN')")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    @PostMapping
    @Operation(summary = "Tạo tài khoản Biên tập viên mới")
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(@Valid @RequestBody AdminRequest request) {
        return new ResponseEntity<>(adminManagementService.createAdmin(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tài khoản nhân viên")
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(@PathVariable Integer id,
            @Valid @RequestBody AdminRequest request) {
        return new ResponseEntity<>(adminManagementService.updateAdmin(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Vô hiệu hóa (xóa mềm) tài khoản nhân viên")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Integer id) {
        return new ResponseEntity<>(adminManagementService.deleteAdmin(id), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin nhân viên theo ID")
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(@PathVariable Integer id) {
        return new ResponseEntity<>(adminManagementService.getAdminById(id), HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách Biên tập viên với chức năng tìm kiếm và phân trang")
    public ApiResponse<List<AdminResponse>> getAllEditors(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "accountId") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        return adminManagementService.getAllEditors(search, pageable);
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('" + PermissionCode.ROLE_ASSIGN + "')")
    @Operation(summary = "Phân công vai trò cụ thể cho người quản trị viên")
    public ResponseEntity<ApiResponse<AdminResponse>> assignRoles(@PathVariable Integer id,
            @Valid @RequestBody RoleAssignmentRequest request) {
        return new ResponseEntity<>(adminManagementService.assignRoles(id, request), HttpStatus.OK);
    }
}
