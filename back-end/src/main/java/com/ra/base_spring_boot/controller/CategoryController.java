package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.req.CategoryRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.CategoryResponseDTO;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import com.ra.base_spring_boot.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Quản lý danh mục")
public class CategoryController {

        private final CategoryService categoryService;

        @GetMapping
        @Operation(summary = "Lấy danh sách tất cả danh mục")
        public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories(
                        @RequestParam(required = false) String search,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sort,
                        @RequestParam(defaultValue = "desc") String direction) {
                return ResponseEntity.ok(categoryService.getAllCategories(search, page, size, sort, direction));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy chi tiết danh mục theo ID")
        public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
                return ResponseEntity.ok(categoryService.getCategoryById(id));
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Tạo danh mục mới", description = "Hỗ trợ truyền mediaId hoặc upload file trực tiếp")
        @PreAuthorize("hasAuthority('" + PermissionCode.CATEGORY_MANAGE + "')")
        @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
        public ResponseEntity<ApiResponse<CategoryResponseDTO>> create(
                        @RequestPart("request") @Valid CategoryRequest request,
                        @RequestPart(value = "file", required = false) MultipartFile file) {
                return new ResponseEntity<>(categoryService.createCategory(request, file), HttpStatus.CREATED);
        }

        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Cập nhật danh mục", description = "Hỗ trợ truyền mediaId hoặc upload file trực tiếp")
        @PreAuthorize("hasAuthority('" + PermissionCode.CATEGORY_MANAGE + "')")
        @RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
        public ResponseEntity<ApiResponse<CategoryResponseDTO>> update(
                        @PathVariable Long id,
                        @RequestPart("request") @Valid CategoryRequest request,
                        @RequestPart(value = "file", required = false) MultipartFile file) {
                return ResponseEntity.ok(categoryService.updateCategory(id, request, file));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Xóa danh mục")
        @PreAuthorize("hasAuthority('" + PermissionCode.CATEGORY_MANAGE + "')")
        public ResponseEntity<ApiResponse<String>> deleteCategory(
                        @PathVariable Long id,
                        @RequestParam(name = "deleteArticles", required = false, defaultValue = "false") Boolean deleteArticles) {
                return ResponseEntity.ok(categoryService.deleteCategory(id, deleteArticles));
        }
}
