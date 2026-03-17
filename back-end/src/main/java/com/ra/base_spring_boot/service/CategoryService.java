package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.CategoryRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.CategoryResponseDTO;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface CategoryService {
    ApiResponse<List<CategoryResponseDTO>> getAllCategories(
            String search,
            int page, int size, String sort, String direction);

    ApiResponse<CategoryResponseDTO> getCategoryById(Long id);

    ApiResponse<CategoryResponseDTO> createCategory(CategoryRequest request,
            MultipartFile file);

    ApiResponse<CategoryResponseDTO> updateCategory(Long id, CategoryRequest request,
            MultipartFile file);

    ApiResponse<String> deleteCategory(Long id, Boolean checkDelete);
}
