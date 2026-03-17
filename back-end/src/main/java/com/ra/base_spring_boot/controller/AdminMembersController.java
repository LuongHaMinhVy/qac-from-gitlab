package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.ApproveAuthorRoleRequest;
import com.ra.base_spring_boot.dto.req.MemberSearchRequest;
import com.ra.base_spring_boot.dto.resp.AdminMemberResponse;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MemberImportResponse;
import com.ra.base_spring_boot.service.adminmember.AdminMembersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
public class AdminMembersController {
        private final AdminMembersService adminMembersService;

        @GetMapping
        public ResponseEntity<ApiResponse<List<AdminMemberResponse>>> searchMembers(
                        @Valid @ModelAttribute MemberSearchRequest request) {
                return ResponseEntity.ok(
                                adminMembersService.getMembersWithPagination(request));
        }

        @GetMapping("/{userId:\\d+}")
        public ResponseEntity<ApiResponse<AdminMemberResponse>> getMemberDetail(
                        @PathVariable Long userId) {
                return ResponseEntity.ok(
                                adminMembersService.getMemberByUserId(userId));
        }

        @PatchMapping("/{userId:\\d+}/status")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<AdminMemberResponse>> updateStatus(
                        @PathVariable Long userId,
                        @RequestParam boolean isActive) {
                return ResponseEntity.ok(
                                adminMembersService.updateMemberStatus(userId, isActive));
        }

        @GetMapping("/author-requests/pending")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
        public ResponseEntity<ApiResponse<?>> getPendingAuthorRequests() {
                return ResponseEntity.ok(
                                adminMembersService.getPendingAuthorRequests());
        }

        @PostMapping("/author-requests/approve")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
        public ResponseEntity<ApiResponse<String>> approveAuthor(
                        @Valid @RequestBody ApproveAuthorRoleRequest request) {
                return ResponseEntity.ok(
                                adminMembersService.approveAuthorRole(request));
        }

        @PostMapping("/author-requests/reject")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
        public ResponseEntity<ApiResponse<String>> rejectAuthor(
                        @Valid @RequestBody ApproveAuthorRoleRequest request) {
                return ResponseEntity.ok(
                                adminMembersService.rejectAuthorRole(request));
        }

        @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EDITOR')")
        public ResponseEntity<ApiResponse<MemberImportResponse>> importExcel(
                        @RequestPart("file") MultipartFile file) {
                return ResponseEntity.ok(
                                adminMembersService.importMembersFromExcel(file));
        }
}
