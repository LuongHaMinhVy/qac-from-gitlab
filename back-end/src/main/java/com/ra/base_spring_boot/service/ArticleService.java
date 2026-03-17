package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.ArticleRejectRequest;
import com.ra.base_spring_boot.dto.req.ArticleRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ApprovalResponse;
import com.ra.base_spring_boot.dto.resp.ArticleResponse;
import com.ra.base_spring_boot.model.constants.ArticleStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ArticleService {

        ApiResponse<List<ArticleResponse>> getAllArticles(String search, ArticleStatus status, Long categoryId,
                        Boolean isHighlight, Boolean isFeatured, String hashtag,
                        int page, int size, String sort, String direction);

        ApiResponse<ArticleResponse> getArticleById(Long id);

        ApiResponse<ArticleResponse> getArticleBySlug(String slug);

        ApiResponse<ArticleResponse> createArticle(ArticleRequest request,
                        MultipartFile file);

        ApiResponse<ArticleResponse> updateArticle(Long id, ArticleRequest request,
                        MultipartFile file);

        ApiResponse<String> deleteArticle(Long id);

        ApiResponse<ArticleResponse> submitForReview(Long id);

        ApiResponse<ArticleResponse> approveArticle(Long id);

        ApiResponse<ArticleResponse> rejectArticle(Long id, ArticleRejectRequest request);

        ApiResponse<ArticleResponse> publishArticle(Long id);

        ApiResponse<ArticleResponse> requestRevision(Long id, ArticleRejectRequest request);

        ApiResponse<List<ApprovalResponse>> getReviewLogs(Long articleId);

        ApiResponse<List<ArticleResponse>> advancedSearch(String search, ArticleStatus status, Long categoryId,
                        Integer authorId, LocalDateTime startDate, LocalDateTime endDate,
                        Boolean isHighlight, Boolean isFeatured, int page, int size, String sort, String direction);
}
