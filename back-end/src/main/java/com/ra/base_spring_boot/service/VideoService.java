package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.VideoRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.VideoResponse;
import com.ra.base_spring_boot.model.constants.VideoStatus;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
        ApiResponse<VideoResponse> createVideo(VideoRequest request, MultipartFile thumbnail, MultipartFile videoFile);

        ApiResponse<VideoResponse> updateVideo(Long id, VideoRequest request,
                        MultipartFile thumbnail, MultipartFile videoFile);

        ApiResponse<String> deleteVideo(Long id);

        ApiResponse<VideoResponse> getVideoById(Long id);

        ApiResponse<VideoResponse> getVideoBySlug(String slug);

        ApiResponse<List<VideoResponse>> getAllVideos(String search, VideoStatus status, Long categoryId,
                        int page,
                        int size, String sort, String direction);
}
