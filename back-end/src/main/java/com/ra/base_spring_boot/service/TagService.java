package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.TagRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ArticleTagListResponse;
import com.ra.base_spring_boot.dto.resp.PopularTagResponse;
import com.ra.base_spring_boot.dto.resp.TagResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TagService {

    ApiResponse<ArticleTagListResponse> addTagsToArticle(Long articleId, TagRequest request);

    ApiResponse<String> removeTagFromArticle(Long articleId, String tag);

    ApiResponse<List<PopularTagResponse>> getPopularTags(int limit);

    ApiResponse<List<String>> getAllTags();

    ApiResponse<ArticleTagListResponse> getTagsByArticleId(Long articleId);

    ApiResponse<List<TagResponse>> searchTags(String keyword, Long articleId, Long categoryId,
            LocalDateTime createdFrom, LocalDateTime createdTo,
            int page, int size, String sortBy, String direction);
}
