package com.ra.base_spring_boot.service.member;

import org.springframework.web.multipart.MultipartFile;

import com.ra.base_spring_boot.dto.req.AuthorRoleRequest;
import com.ra.base_spring_boot.dto.req.MemberRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MemberResponse;

public interface MemberService {
    ApiResponse<MemberResponse> getCurrentMember();

    ApiResponse<MemberResponse> updateMember(MemberRequest request,
            MultipartFile file);

    ApiResponse<String> requestAuthorRole(AuthorRoleRequest request);
}
