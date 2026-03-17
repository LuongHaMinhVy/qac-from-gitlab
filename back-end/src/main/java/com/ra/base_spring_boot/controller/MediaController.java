package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MediaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.service.MediaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Quản lý phương tiện")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Tải lên phương tiện", description = "Hỗ trợ tải lên Hình ảnh, GIF, và Video. Tự động nén và tối ưu hóa.")
    @PreAuthorize("hasAuthority('" + PermissionCode.MEDIA_MANAGE + "')")
    public ResponseEntity<ApiResponse<MediaResponseDTO>> upload(
            @Parameter(description = "Tệp tin cần tải lên (Image/GIF < 10MB, Video < 50MB)", content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "string", format = "binary"))) @RequestParam("file") MultipartFile file) {
        MediaResponseDTO media = mediaService.upload(file);
        return new ResponseEntity<>(ApiResponse.success(media, "Tải lên thành công"), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCode.MEDIA_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long id) {
        mediaService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa phương tiện thành công"));
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('" + PermissionCode.MEDIA_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable Long id) {
        mediaService.restore(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Khôi phục phương tiện thành công"));
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<ApiResponse<Map<String, String>>> getThumbnail(@PathVariable Long id) {
        String thumbnailUrl = mediaService.getThumbnailUrl(id);
        Map<String, String> response = new HashMap<>();
        response.put("thumbnailUrl", thumbnailUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy ảnh thu nhỏ thành công"));
    }

    @GetMapping
    @Operation(summary = "Tìm kiếm và lọc phương tiện", description = "Tìm kiếm theo từ khóa (tên tệp, tên gốc, mô tả, chú thích), lọc theo loại MIME và thời gian tạo.")
    public ResponseEntity<ApiResponse<List<MediaResponseDTO>>> search(
            @Parameter(description = "Từ khóa tìm kiếm (tên tệp, alt, caption...)", example = "") @RequestParam(required = false) String keyword,
            @Parameter(description = "Loại MIME (image, video, v.v.)", example = "image") @RequestParam(required = false) String mimeType,
            @Parameter(description = "Ngày tạo từ (ISO 8601)", example = "2023-01-01T00:00:00") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @Parameter(description = "Ngày tạo đến (ISO 8601)", example = "2025-12-31T23:59:59") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @Parameter(description = "Chỉ số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp (id, fileName, createdAt...)", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc: tăng dần, desc: giảm dần)", example = "desc") @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity
                .ok(mediaService.findAll(keyword, mimeType, createdFrom, createdTo, page, size, sortBy, direction));
    }
}
