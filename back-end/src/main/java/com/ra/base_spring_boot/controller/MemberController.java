package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.AuthorRoleRequest;
import com.ra.base_spring_boot.dto.req.MemberRequest;
import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MemberResponse;
import com.ra.base_spring_boot.service.ActivitiLogService;
import com.ra.base_spring_boot.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MemberController {

    private final MemberService memberService;
    private final ActivitiLogService activityLogService;

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getCurrentMember() {
        return memberService.getCurrentMember();
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật thông tin cá nhân", description = "Hỗ trợ upload ảnh đại diện trực tiếp hoặc truyền mediaId")
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ApiResponse<MemberResponse> updateMember(
            @RequestPart("request") @Valid MemberRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return memberService.updateMember(request, file);
    }

    @PostMapping("/request-author-role")
    public ApiResponse<String> requestAuthorRole(@Valid @RequestBody AuthorRoleRequest request) {
        return memberService.requestAuthorRole(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.ACTIVITY_LOG_VIEW + "')")
    public ApiResponse<List<ActivityLogResponseDTO>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,

            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword) {
        return activityLogService.getLogs(page, size, sortBy, sortDir, accountId, action, keyword);
    }
}
