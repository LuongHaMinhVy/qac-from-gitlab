package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.BadWordRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.model.BadWord;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.service.ContentModerationService;
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
@RequestMapping("/api/v1/bad-words")
@RequiredArgsConstructor
@Tag(name = "Bad Words", description = "Quản lý từ khóa cấm / không phù hợp")
public class BadWordController {

    private final ContentModerationService contentModerationService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.BADWORD_MANAGE + "')")
    @Operation(summary = "Lấy danh sách từ khóa cấm")
    public ResponseEntity<ApiResponse<List<BadWord>>> getAllBadWords() {
        return ResponseEntity.ok(contentModerationService.getAllBadWords());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.BADWORD_MANAGE + "')")
    @Operation(summary = "Thêm từ khóa cấm")
    public ResponseEntity<ApiResponse<BadWord>> addBadWord(@Valid @RequestBody BadWordRequest request) {
        return new ResponseEntity<>(contentModerationService.addBadWord(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.BADWORD_MANAGE + "')")
    @Operation(summary = "Cập nhật từ khóa cấm")
    public ResponseEntity<ApiResponse<BadWord>> updateBadWord(@PathVariable Long id,
            @Valid @RequestBody BadWordRequest request) {
        return ResponseEntity.ok(contentModerationService.updateBadWord(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.BADWORD_MANAGE + "')")
    @Operation(summary = "Xóa từ khóa cấm")
    public ResponseEntity<ApiResponse<String>> deleteBadWord(@PathVariable Long id) {
        return ResponseEntity.ok(contentModerationService.deleteBadWord(id));
    }
}
