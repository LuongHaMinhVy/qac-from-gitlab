package com.ra.base_spring_boot.service.adminmember;

import com.ra.base_spring_boot.dto.req.ApproveAuthorRoleRequest;
import com.ra.base_spring_boot.dto.req.MemberSearchRequest;
import com.ra.base_spring_boot.dto.resp.AdminMemberResponse;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MemberImportResponse;
import com.ra.base_spring_boot.dto.resp.RoleRequestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminMembersService {
    ApiResponse<List<RoleRequestResponse>> getPendingAuthorRequests();
    ApiResponse<String> approveAuthorRole(ApproveAuthorRoleRequest request);
    ApiResponse<String> rejectAuthorRole(ApproveAuthorRoleRequest request);
    ApiResponse<MemberImportResponse> importMembersFromExcel(MultipartFile file);
    ApiResponse<AdminMemberResponse> getMemberByUserId(Long userId);
    ApiResponse<AdminMemberResponse> updateMemberStatus(Long userId, boolean isActive);
    ApiResponse<List<AdminMemberResponse>> getMembersWithPagination(MemberSearchRequest request);
}