package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.VideoRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.VideoResponse;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.model.constants.VideoStatus;
import com.ra.base_spring_boot.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Tag(name = "Video", description = "Quản lý tin Video")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + PermissionCode.VIDEO_CREATE + "')")
    @Operation(summary = "Tạo video mới", description = "Tạo video mới (YouTube, Vimeo hoặc Upload).")
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<VideoResponse>> createVideo(
            @RequestPart("request") @Valid VideoRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {
        return new ResponseEntity<>(videoService.createVideo(request, thumbnail, videoFile), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('" + PermissionCode.VIDEO_UPDATE_OWN + "', '" + PermissionCode.VIDEO_UPDATE_ALL
            + "')")
    @Operation(summary = "Cập nhật video", description = "Cập nhật thông tin video.")
    @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    public ResponseEntity<ApiResponse<VideoResponse>> updateVideo(
            @PathVariable Long id,
            @RequestPart("request") @Valid VideoRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {
        return new ResponseEntity<>(videoService.updateVideo(id, request, thumbnail, videoFile), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.VIDEO_DELETE + "')")
    @Operation(summary = "Xóa video", description = "Xóa video. Tác giả chỉ được xóa bài của mình.")
    public ResponseEntity<ApiResponse<String>> deleteVideo(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.deleteVideo(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết video theo ID")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideoById(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Lấy chi tiết video theo Slug")
    public ResponseEntity<ApiResponse<VideoResponse>> getVideoBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(videoService.getVideoBySlug(slug));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách video", description = "Lọc, tìm kiếm và phân trang video")
    public ResponseEntity<ApiResponse<List<VideoResponse>>> getAllVideos(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) VideoStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(videoService.getAllVideos(search, status, categoryId, page, size, sort, direction));
    }
}
