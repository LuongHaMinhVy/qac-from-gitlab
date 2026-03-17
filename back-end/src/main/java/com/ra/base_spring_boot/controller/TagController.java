package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.TagRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ArticleTagListResponse;
import com.ra.base_spring_boot.dto.resp.PopularTagResponse;
import com.ra.base_spring_boot.dto.resp.TagResponse;
import com.ra.base_spring_boot.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "API quản lý tags cho bài viết")
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    @Operation(summary = "Lấy tất cả tags", description = "Lấy danh sách tất cả tags duy nhất trong hệ thống")
    public ResponseEntity<ApiResponse<List<String>>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/tags/popular")
    @Operation(summary = "Lấy tags phổ biến", description = "Lấy danh sách tags phổ biến nhất với số lượng bài viết")
    public ResponseEntity<ApiResponse<List<PopularTagResponse>>> getPopularTags(
            @Parameter(description = "Số lượng tags muốn lấy", example = "10") @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(tagService.getPopularTags(limit));
    }

    @GetMapping("/articles/{articleId}/tags")
    @Operation(summary = "Lấy tags của bài viết", description = "Lấy danh sách tags của một bài viết cụ thể")
    public ResponseEntity<ApiResponse<ArticleTagListResponse>> getTagsByArticleId(
            @Parameter(description = "ID của bài viết") @PathVariable Long articleId) {
        return ResponseEntity.ok(tagService.getTagsByArticleId(articleId));
    }

    @PostMapping("/articles/{articleId}/tags")
    @Operation(summary = "Thêm tags vào bài viết", description = "Thêm một hoặc nhiều tags vào bài viết. Tags đã tồn tại sẽ được bỏ qua.")
    @PreAuthorize("hasAuthority('" + PermissionCode.TAG_MANAGE + "')")
    public ResponseEntity<ApiResponse<ArticleTagListResponse>> addTagsToArticle(
            @Parameter(description = "ID của bài viết") @PathVariable Long articleId,
            @Valid @RequestBody TagRequest request) {
        return new ResponseEntity<>(tagService.addTagsToArticle(articleId, request), HttpStatus.CREATED);
    }

    @DeleteMapping("/articles/{articleId}/tags/{tag}")
    @Operation(summary = "Xóa tag khỏi bài viết", description = "Xóa một tag cụ thể khỏi bài viết")
    @PreAuthorize("hasAuthority('" + PermissionCode.TAG_MANAGE + "')")
    public ResponseEntity<ApiResponse<String>> removeTagFromArticle(
            @Parameter(description = "ID của bài viết") @PathVariable Long articleId,
            @Parameter(description = "Tên tag cần xóa") @PathVariable String tag) {
        return ResponseEntity.ok(tagService.removeTagFromArticle(articleId, tag));
    }

    @GetMapping("/tags/search")
    @Operation(summary = "Tìm kiếm và lọc tags", description = "Tìm kiếm tags theo nhiều tiêu chí với thông tin chi tiết")
    public ResponseEntity<ApiResponse<List<TagResponse>>> searchTags(
            @Parameter(description = "Từ khóa tìm kiếm", example = "java") @RequestParam(required = false) String keyword,
            @Parameter(description = "Lọc theo ID bài viết") @RequestParam(required = false) Long articleId,
            @Parameter(description = "Lọc theo ID danh mục") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Ngày tạo từ (ISO 8601)", example = "2023-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @Parameter(description = "Ngày tạo đến (ISO 8601)", example = "2025-12-31T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @Parameter(description = "Số trang", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp (id, tag, createdAt)", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC, DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        return ResponseEntity.ok(tagService.searchTags(keyword, articleId, categoryId, createdFrom, createdTo, page,
                size, sortBy, direction));
    }
}
