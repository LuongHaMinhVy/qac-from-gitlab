package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.ArticleRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ArticleResponse;
import com.ra.base_spring_boot.model.constants.ArticleStatus;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Tag(name = "Article", description = "Quản lý bài viết")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> getAllArticles(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isHighlight,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) String hashtag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity
                .ok(articleService.getAllArticles(search, status, categoryId, isHighlight, isFeatured, hashtag,
                        page, size, sort, direction));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm nâng cao", description = "Tìm kiếm bài viết theo nội dung, tác giả, khoảng thời gian")
    public ResponseEntity<ApiResponse<List<ArticleResponse>>> advancedSearch(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer authorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {
                    "yyyy-MM-dd", "dd-MM-yyyy", "d-M-yyyy" }) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, fallbackPatterns = {
                    "yyyy-MM-dd", "dd-MM-yyyy", "d-M-yyyy" }) LocalDateTime endDate,
            @RequestParam(required = false) Boolean isHighlight,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(articleService.advancedSearch(search, status, categoryId, authorId, startDate, endDate,
                isHighlight, isFeatured, page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + PermissionCode.ARTICLE_CREATE + "')")
    @Operation(summary = "Tạo bài viết mới", description = "Hỗ trợ truyền mediaId hoặc upload file trực tiếp")
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(
            @RequestPart("request") @Valid ArticleRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return new ResponseEntity<>(articleService.createArticle(request, file), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật bài viết", description = "Hỗ trợ truyền mediaId hoặc upload file trực tiếp")
    @PreAuthorize("hasAnyAuthority('" + PermissionCode.ARTICLE_UPDATE_OWN + "', '" + PermissionCode.ARTICLE_UPDATE_ALL
            + "')")
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(@PathVariable Long id,
            @RequestPart("request") @Valid ArticleRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return new ResponseEntity<>(articleService.updateArticle(id, request, file), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.ARTICLE_DELETE + "')")
    public ResponseEntity<ApiResponse<String>> deleteArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.deleteArticle(id));
    }
}
