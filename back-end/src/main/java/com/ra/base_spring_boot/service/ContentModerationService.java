package com.ra.base_spring_boot.service;

public interface ContentModerationService {

    void validateContent(String content);

    String sanitizeContent(String content);

    boolean containsToxicContent(String content);

    void refreshBadWordsCache();

    com.ra.base_spring_boot.dto.resp.ApiResponse<com.ra.base_spring_boot.model.BadWord> addBadWord(
            com.ra.base_spring_boot.dto.req.BadWordRequest request);

    com.ra.base_spring_boot.dto.resp.ApiResponse<com.ra.base_spring_boot.model.BadWord> updateBadWord(Long id,
            com.ra.base_spring_boot.dto.req.BadWordRequest request);

    com.ra.base_spring_boot.dto.resp.ApiResponse<String> deleteBadWord(Long id);

    com.ra.base_spring_boot.dto.resp.ApiResponse<java.util.List<com.ra.base_spring_boot.model.BadWord>> getAllBadWords();
}
