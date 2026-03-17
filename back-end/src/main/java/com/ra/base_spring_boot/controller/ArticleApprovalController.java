package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.ArticleRejectRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ArticleResponse;
import com.ra.base_spring_boot.dto.resp.ApprovalResponse;
import com.ra.base_spring_boot.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Tag(name = "Article Approval", description = "Quản lý phê duyệt bài viết")
public class ArticleApprovalController {

    private final ArticleService articleService;

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ARTICLE_CREATE', 'ARTICLE_UPDATE_OWN', 'ARTICLE_UPDATE_ALL')")
    @Operation(summary = "Gửi bài viết để duyệt", description = "Chuyển trạng thái từ Nháp sang Chờ duyệt")
    public ResponseEntity<ApiResponse<ArticleResponse>> submitForReview(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.submitForReview(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ARTICLE_APPROVE')")
    @Operation(summary = "Phê duyệt bài viết", description = "Duyệt bài viết đang chờ (Quyền Admin/Editor)")
    public ResponseEntity<ApiResponse<ArticleResponse>> approveArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.approveArticle(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ARTICLE_APPROVE')")
    @Operation(summary = "Từ chối bài viết", description = "Từ chối bài viết kèm lý do (Quyền Admin/Editor)")
    public ResponseEntity<ApiResponse<ArticleResponse>> rejectArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRejectRequest request) {
        return ResponseEntity.ok(articleService.rejectArticle(id, request));
    }

    @PutMapping("/{id}/request-revision")
    @PreAuthorize("hasAuthority('ARTICLE_APPROVE')")
    @Operation(summary = "Yêu cầu sửa đổi", description = "Yêu cầu tác giả chỉnh sửa lại bài viết (Quyền Admin/Editor)")
    public ResponseEntity<ApiResponse<ArticleResponse>> requestRevision(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRejectRequest request) {
        return ResponseEntity.ok(articleService.requestRevision(id, request));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ARTICLE_PUBLISH')")
    @Operation(summary = "Công khai bài viết", description = "Đưa bài viết đã duyệt lên trang chủ")
    public ResponseEntity<ApiResponse<ArticleResponse>> publishArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.publishArticle(id));
    }

    @GetMapping("/{id}/review-logs")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem lịch sử phê duyệt", description = "Lấy danh sách nhật ký thay đổi trạng thái của bài viết")
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getReviewLogs(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getReviewLogs(id));
    }
}
