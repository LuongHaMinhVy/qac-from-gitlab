package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.CategoryRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.CategoryResponseDTO;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.exception.HttpUnAuthorized;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Category;
import com.ra.base_spring_boot.model.Media;
import com.ra.base_spring_boot.repository.ArticleRepository;
import com.ra.base_spring_boot.repository.CategoryRepository;
import com.ra.base_spring_boot.service.CategoryService;
import com.ra.base_spring_boot.service.MediaService;
import com.ra.base_spring_boot.service.ContentModerationService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

        private final CategoryRepository categoryRepository;
        private final ArticleRepository articleRepository;
        private final MediaService mediaService;
        private final ContentModerationService contentModerationService;

        private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
        private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

        @Override
        public ApiResponse<List<CategoryResponseDTO>> getAllCategories(
                        String search,
                        int page, int size, String sort, String direction) {

                Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

                String keyword = (search == null || search.trim().isEmpty()) ? null : search.trim();

                Page<Category> result = categoryRepository.findAllWithFilter(keyword, pageable);

                List<CategoryResponseDTO> items = result.getContent().stream()
                                .map(category -> CategoryResponseDTO.builder()
                                                .id(category.getId())
                                                .name(category.getName())
                                                .description(category.getDescription())
                                                .coverImage(category.getCoverImage() != null
                                                                ? mediaService.convertToDTO(category.getCoverImage())
                                                                : null)
                                                .displayOrder(category.getDisplayOrder())

                                                .build())
                                .toList();

                Pagination pagination = Pagination.builder()
                                .currentPage(result.getNumber())
                                .pageSize(result.getSize())
                                .totalElements(result.getTotalElements())
                                .totalPages(result.getTotalPages())
                                .build();

                return ApiResponse.success(items, "Lấy danh sách danh mục thành công", pagination);
        }

        @Override
        public ApiResponse<CategoryResponseDTO> getCategoryById(Long id) {
                Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy danh mục"));
                CategoryResponseDTO result = CategoryResponseDTO
                                .builder()
                                .id(category.getId())
                                .name(category.getName())
                                .description(category.getDescription())
                                .coverImage(category.getCoverImage() != null
                                                ? mediaService.convertToDTO(category.getCoverImage())
                                                : null)
                                .displayOrder(category.getDisplayOrder())
                                .status(category.getStatus())
                                .createdAt(category.getCreatedAt())
                                .build();
                return ApiResponse
                                .<CategoryResponseDTO>builder()
                                .success(true)
                                .message("Lấy danh mục ID: " + result.getId() + " thành công")
                                .data(result)
                                .pagination(null)
                                .errors(null)
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        @Override
        @Transactional
        public ApiResponse<CategoryResponseDTO> createCategory(CategoryRequest request,
                        MultipartFile file) {

                Account currentAccount = SecurityUtils.getCurrentAccount();
                if (currentAccount == null) {
                        throw new HttpUnAuthorized("Người dùng chưa xác thực");
                }

                contentModerationService.validateContent(request.getName());
                contentModerationService.validateContent(request.getDescription());

                Media coverImage = null;
                if (file != null && !file.isEmpty()) {
                        coverImage = mediaService.uploadFile(file);
                } else if (request.getMediaId() != null) {
                        coverImage = mediaService.findById(request.getMediaId());
                }

                Category category = Category.builder()
                                .name(request.getName())
                                .slug(toSlug(request.getName()))
                                .description(request.getDescription())
                                .coverImage(coverImage)
                                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                                .status(request.getStatus() != null ? request.getStatus() : true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy(currentAccount)
                                .build();

                Category savedCategory = categoryRepository.save(category);
                CategoryResponseDTO categoryResponse = CategoryResponseDTO.builder()
                                .id(savedCategory.getId())
                                .name(savedCategory.getName())
                                .description(savedCategory.getDescription())
                                .coverImage(savedCategory.getCoverImage() != null
                                                ? mediaService.convertToDTO(savedCategory.getCoverImage())
                                                : null)
                                .displayOrder(savedCategory.getDisplayOrder())
                                .status(savedCategory.getStatus())
                                .createdAt(savedCategory.getCreatedAt())
                                .build();

                return ApiResponse.<CategoryResponseDTO>builder()
                                .success(true)
                                .message("Tạo danh mục thành công")
                                .data(categoryResponse)
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        @Override
        @Transactional
        public ApiResponse<CategoryResponseDTO> updateCategory(Long id, CategoryRequest request,
                        MultipartFile file) {

                Account currentAccount = SecurityUtils.getCurrentAccount();
                if (currentAccount == null) {
                        throw new HttpUnAuthorized("Người dùng chưa xác thực");
                }

                Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy danh mục"));

                if (file != null && !file.isEmpty()) {
                        Media media = mediaService.uploadFile(file);
                        category.setCoverImage(media);
                } else if (request.getMediaId() != null) {
                        category.setCoverImage(mediaService.findById(request.getMediaId()));
                }
                if (request.getName() != null && !request.getName().isBlank()) {
                        contentModerationService.validateContent(request.getName());
                        category.setName(request.getName());
                        category.setSlug(toSlug(request.getName()));
                }
                if (request.getDescription() != null) {
                        contentModerationService.validateContent(request.getDescription());
                        category.setDescription(request.getDescription());
                }
                if (request.getDisplayOrder() != null) {
                        category.setDisplayOrder(request.getDisplayOrder());
                }
                if (request.getStatus() != null) {
                        category.setStatus(request.getStatus());
                }
                category.setUpdatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                category.setCreatedAt(category.getCreatedAt());
                category.setCreatedBy(currentAccount);

                Category savedCategory = categoryRepository.save(category);
                CategoryResponseDTO response = CategoryResponseDTO
                                .builder()
                                .id(savedCategory.getId())
                                .name(savedCategory.getName())
                                .description(savedCategory.getDescription())
                                .coverImage(savedCategory.getCoverImage() != null
                                                ? mediaService.convertToDTO(savedCategory.getCoverImage())
                                                : null)
                                .displayOrder(savedCategory.getDisplayOrder())
                                .status(savedCategory.getStatus())
                                .createdAt(savedCategory.getCreatedAt())
                                .build();
                return ApiResponse.<CategoryResponseDTO>builder()
                                .success(true)
                                .message("Cập nhật danh mục thành công")
                                .data(response)
                                .timestamp(LocalDateTime.now())
                                .build();
        }

        @Override
        @Transactional
        public ApiResponse<String> deleteCategory(Long id, Boolean deleteArticles) {
                Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy danh mục"));

                if (Boolean.TRUE.equals(deleteArticles)) {
                        articleRepository.deleteAllByCategoryId(id);
                        categoryRepository.delete(category);
                        return ApiResponse.success(null, "Xóa danh mục và tất cả bài viết liên quan thành công");
                }

                articleRepository.clearCategoryByCategoryId(id);
                categoryRepository.delete(category);

                return ApiResponse.success(null,
                                "Đã xóa danh mục, các bài viết liên quan đã được chuyển sang mục không phân loại");
        }

        private String toSlug(String input) {
                String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
                String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
                String slug = NONLATIN.matcher(normalized).replaceAll("");
                return slug.toLowerCase(Locale.ENGLISH);
        }
}
