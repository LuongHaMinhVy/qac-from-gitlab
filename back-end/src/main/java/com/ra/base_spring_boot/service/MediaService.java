package com.ra.base_spring_boot.service;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MediaResponseDTO;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Media;

public interface MediaService {
    MediaResponseDTO upload(MultipartFile file);

    Media uploadFile(MultipartFile file);

    void softDelete(Long id);

    void restore(Long id);

    String getThumbnailUrl(Long id);

    ApiResponse<List<MediaResponseDTO>> findAll(String keyword, String mimeType,
            LocalDateTime createdFrom, LocalDateTime createdTo,
            int page, int size, String sortBy, String direction);

    Media findById(Long id);

    MediaResponseDTO convertToDTO(Media media);

    Media createExternal(String url, Account uploader);

    Media createExternal(String url, String originalName, Account uploader);
}
