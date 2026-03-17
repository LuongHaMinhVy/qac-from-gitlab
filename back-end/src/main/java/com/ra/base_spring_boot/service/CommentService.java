package com.ra.base_spring_boot.service;

import java.util.List;

import com.ra.base_spring_boot.dto.req.CommentRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.CommentResponse;

public interface CommentService {
        ApiResponse<CommentResponse> createComment(CommentRequest request);

        ApiResponse<CommentResponse> updateComment(Long id, CommentRequest request);

        ApiResponse<String> deleteComment(Long id);

        ApiResponse<List<CommentResponse>> getCommentsByArticleId(Long articleId, int page, int size,
                        String sort,
                        String direction);

        ApiResponse<List<CommentResponse>> getCommentsByVideoId(Long videoId, int page, int size, String sort,
                        String direction);
}
