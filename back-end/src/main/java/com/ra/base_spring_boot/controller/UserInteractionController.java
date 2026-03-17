package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.UserInteractionRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.service.UserInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interactions")
@RequiredArgsConstructor
@Tag(name = "User Interaction", description = "Tương tác người dùng (Like/Dislike)")
public class UserInteractionController {

    private final UserInteractionService userInteractionService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tương tác (Like/Dislike)", description = "Thêm, cập nhật hoặc xóa tương tác")
    public ResponseEntity<ApiResponse<String>> interact(@Valid @RequestBody UserInteractionRequest request) {
        return ResponseEntity.ok(userInteractionService.interact(request));
    }
}
