package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.AdminRequest;
import com.ra.base_spring_boot.dto.req.RoleAssignmentRequest;
import com.ra.base_spring_boot.dto.resp.AdminResponse;
import com.ra.base_spring_boot.dto.resp.ApiResponse;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface AdminManagementService {
    ApiResponse<AdminResponse> createAdmin(AdminRequest request);

    ApiResponse<AdminResponse> updateAdmin(Integer id, AdminRequest request);

    ApiResponse<String> deleteAdmin(Integer id);

    ApiResponse<AdminResponse> getAdminById(Integer id);

    ApiResponse<List<AdminResponse>> getAllEditors(String search, Pageable pageable);

    ApiResponse<AdminResponse> assignRoles(Integer id, RoleAssignmentRequest request);
}
