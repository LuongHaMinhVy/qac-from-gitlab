package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.CommentRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.CommentResponse;
import com.ra.base_spring_boot.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "Quản lý bình luận")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCode.COMMENT_CREATE + "')")
    @Operation(summary = "Tạo bình luận mới", description = "Bình luận cho bài viết hoặc video")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@Valid @RequestBody CommentRequest request) {
        return new ResponseEntity<>(commentService.createComment(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.COMMENT_UPDATE_OWN + "')")
    @Operation(summary = "Cập nhật bình luận", description = "Chỉnh sửa nội dung bình luận (chỉ tác giả)")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(@PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + PermissionCode.COMMENT_DELETE_OWN + "', '" + PermissionCode.COMMENT_DELETE_ALL
            + "')")
    @Operation(summary = "Xóa bình luận", description = "Xóa bình luận (tác giả hoặc admin/editor)")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.deleteComment(id));
    }

    @GetMapping("/article/{articleId}")
    @Operation(summary = "Lấy danh sách bình luận của bài viết")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByArticleId(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(commentService.getCommentsByArticleId(articleId, page, size, sort, direction));
    }

    @GetMapping("/video/{videoId}")
    @Operation(summary = "Lấy danh sách bình luận của video")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByVideoId(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(commentService.getCommentsByVideoId(videoId, page, size, sort, direction));
    }
}
