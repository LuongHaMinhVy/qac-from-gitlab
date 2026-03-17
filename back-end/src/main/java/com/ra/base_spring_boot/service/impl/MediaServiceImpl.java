package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.cloudinary.CloudinaryService;
import com.ra.base_spring_boot.dto.resp.AccountResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MediaResponseDTO;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.exception.HttpUnAuthorized;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Media;
import com.ra.base_spring_boot.model.Role;
import com.ra.base_spring_boot.repository.MediaRepository;
import com.ra.base_spring_boot.service.MediaService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<MediaResponseDTO>> findAll(String keyword, String mimeType,
            LocalDateTime createdFrom, LocalDateTime createdTo, int page, int size, String sortBy, String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Media> mediaPage = mediaRepository.findAllWithFilter(keyword, mimeType, createdFrom, createdTo, pageable);
        List<MediaResponseDTO> dtoList = mediaPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Pagination pagination = Pagination.builder()
                .currentPage(mediaPage.getNumber())
                .pageSize(mediaPage.getSize())
                .totalElements(mediaPage.getTotalElements())
                .totalPages(mediaPage.getTotalPages())
                .build();

        ApiResponse<List<MediaResponseDTO>> response = ApiResponse.success(dtoList,
                "Lấy danh sách phương tiện thành công");
        response.setPagination(pagination);
        return response;
    }

    @Override
    @Transactional
    public MediaResponseDTO upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        Optional<Media> existing = mediaRepository.findByOriginalNameAndFileSizeAndIsDeletedFalse(originalFilename,
                file.getSize());

        if (existing.isPresent()) {
            MediaResponseDTO dto = convertToDTO(existing.get());
            dto.setDeduplicated(true);
            return dto;
        }

        Media savedMedia = uploadFile(file);
        MediaResponseDTO dto = convertToDTO(savedMedia);
        dto.setDeduplicated(false);
        return dto;
    }

    @Override
    @Transactional
    public Media uploadFile(MultipartFile file) {
        Account currentAccount = SecurityUtils.getCurrentAccount();
        if (currentAccount == null) {
            throw new HttpUnAuthorized("Người dùng phải đăng nhập để tải lên phương tiện");
        }

        if (file.isEmpty()) {
            throw new HttpBadRequest("Tệp tin không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/")
                && !contentType.equals("image/gif"))) {
            throw new HttpBadRequest("Chỉ hỗ trợ tải lên hình ảnh, GIF và video");
        }

        long maxFileSize = contentType.startsWith("video/") ? 50 * 1024 * 1024 : 10 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            throw new HttpBadRequest("Kích thước tệp tin vượt quá giới hạn cho phép");
        }

        String originalFilename = file.getOriginalFilename();

        Optional<Media> existingMedia = mediaRepository.findByOriginalNameAndFileSizeAndIsDeletedFalse(originalFilename,
                file.getSize());
        if (existingMedia.isPresent()) {
            return existingMedia.get();
        }

        Map uploadResult;
        try {
            uploadResult = cloudinaryService.uploadFile(file);
        } catch (IOException e) {
            throw new HttpBadRequest("Lỗi khi tải tệp tin lên máy chủ: " + e.getMessage());
        }

        String url = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");
        Long bytes = Long.parseLong(uploadResult.get("bytes").toString());
        String format = (String) uploadResult.get("format");
        String resourceType = (String) uploadResult.get("resource_type");

        Media media = Media.builder()
                .fileName(originalFilename)
                .originalName(originalFilename)
                .fileUrl(url)
                .publicId(publicId)
                .mimeType(resourceType + "/" + format)
                .fileSize(bytes)
                .uploader(currentAccount)
                .isDeleted(false)
                .build();

        Media savedMedia = mediaRepository.save(media);
        return savedMedia;
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy phương tiện với id: " + id));

        Account current = SecurityUtils.getCurrentAccount();
        if (current == null || (!media.getUploader().getAccountId().equals(current.getAccountId())
                && !hasRole(current, "ROLE_ADMIN"))) {
            throw new HttpUnAuthorized("Không có quyền xóa phương tiện này");
        }

        media.setIsDeleted(true);
        media.setDeletedAt(LocalDateTime.now());
        mediaRepository.save(media);
    }

    @Override
    @Transactional
    public void restore(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy phương tiện với id: " + id));

        Account current = SecurityUtils.getCurrentAccount();
        if (current == null || (!media.getUploader().getAccountId().equals(current.getAccountId())
                && !hasRole(current, "ROLE_ADMIN"))) {
            throw new HttpUnAuthorized("Không có quyền phục hồi phương tiện này");
        }

        media.setIsDeleted(false);
        media.setDeletedAt(null);
        mediaRepository.save(media);
    }

    @Override
    public String getThumbnailUrl(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy phương tiện với id: " + id));

        if (media.getIsDeleted()) {
            throw new HttpNotFound("Phương tiện đã bị xóa");
        }

        String url = media.getFileUrl();
        if (url == null || !url.contains("/upload/")) {
            return url;
        }

        if (media.getMimeType().startsWith("video/")) {
            return url.replace("/upload/", "/upload/w_300,h_200,c_fill,q_auto,f_jpg,so_auto/");
        }

        return url.replace("/upload/", "/upload/w_150,h_150,c_fill,q_auto,f_auto/");
    }

    @Override
    public Media findById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy phương tiện với id: " + id));

        if (media.getIsDeleted()) {
            throw new HttpNotFound("Phương tiện đã bị xóa");
        }

        return media;
    }

    @Override
    @Transactional
    public Media createExternal(String url, String originalName, Account uploader) {
        if (url == null || url.trim().isEmpty()) {
            throw new HttpBadRequest("URL không được để trống");
        }

        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf('?'));
        }
        if (fileName.isEmpty()) {
            fileName = "external_media";
        }

        String mimeType = "application/octet-stream";
        if (url.matches(".*\\.(jpg|jpeg|png|gif|bmp)$")) {
            mimeType = "image/" + url.substring(url.lastIndexOf('.') + 1);
        } else if (url.matches(".*\\.(mp4|webm|ogg)$")) {
            mimeType = "video/" + url.substring(url.lastIndexOf('.') + 1);
        }

        Media media = Media.builder()
                .fileName(fileName)
                .originalName(originalName != null ? originalName : fileName)
                .fileUrl(url)
                .publicId(null)
                .mimeType(mimeType)
                .fileSize(0L)
                .uploader(uploader)
                .isDeleted(false)
                .build();

        return mediaRepository.save(media);
    }

    @Override
    @Transactional
    public Media createExternal(String url, Account uploader) {
        return createExternal(url, null, uploader);
    }

    @Override
    public MediaResponseDTO convertToDTO(Media media) {
        AccountResponseDTO uploaderDTO = null;
        if (media.getUploader() != null) {
            uploaderDTO = AccountResponseDTO.builder()
                    .id(media.getUploader().getAccountId())
                    .username(media.getUploader().getUsername())
                    .email(media.getUploader().getEmail())
                    .status(media.getUploader().getIsActive())
                    .roles(media.getUploader().getRoles().stream()
                            .map(Role::getRoleName)
                            .collect(Collectors.toList()))
                    .build();
        }

        return MediaResponseDTO.builder()
                .id(media.getId())
                .fileName(media.getFileName())
                .originalName(media.getOriginalName())
                .fileUrl(media.getFileUrl())
                .mimeType(media.getMimeType())
                .fileSize(media.getFileSize())
                .altText(media.getAltText())
                .caption(media.getCaption())
                .uploader(uploaderDTO)
                .createdAt(media.getCreatedAt())
                .isDeleted(media.getIsDeleted())
                .deletedAt(media.getDeletedAt())
                .build();
    }

    private boolean hasRole(Account account, String roleName) {
        if (account == null || account.getRoles() == null)
            return false;
        return account.getRoles().stream()
                .anyMatch(r -> r.getRoleName().equals(roleName));
    }
}
