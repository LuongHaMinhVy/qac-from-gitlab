package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.TagRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ArticleTagListResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.dto.resp.PopularTagResponse;
import com.ra.base_spring_boot.dto.resp.TagResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Article;
import com.ra.base_spring_boot.model.ArticleTag;
import com.ra.base_spring_boot.repository.ArticleRepository;
import com.ra.base_spring_boot.repository.ArticleTagRepository;
import com.ra.base_spring_boot.repository.CategoryRepository;
import com.ra.base_spring_boot.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final ArticleTagRepository articleTagRepository;
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ApiResponse<ArticleTagListResponse> addTagsToArticle(Long articleId, TagRequest request) {
        if (articleId == null || articleId <= 0) {
            throw new HttpBadRequest("ID bài viết không hợp lệ");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết với id: " + articleId));

        if (request.getTags() == null || request.getTags().isEmpty()) {
            throw new HttpBadRequest("Danh sách tags không được để trống");
        }

        List<String> addedTags = new ArrayList<>();
        List<String> skippedTags = new ArrayList<>();

        for (String tag : request.getTags()) {
            if (tag == null)
                continue;

            String trimmedTag = tag.trim();
            if (trimmedTag.isEmpty())
                continue;

            if (!articleTagRepository.existsByArticleIdAndTagIgnoreCase(articleId, trimmedTag)) {
                ArticleTag articleTag = ArticleTag.builder()
                        .article(article)
                        .tag(trimmedTag)
                        .createdAt(LocalDateTime.now())
                        .build();
                articleTagRepository.save(articleTag);
                addedTags.add(trimmedTag);
            } else {
                skippedTags.add(trimmedTag);
            }
        }

        List<String> allTags = articleTagRepository.findAllByArticleId(articleId)
                .stream()
                .map(ArticleTag::getTag)
                .collect(Collectors.toList());

        ArticleTagListResponse response = ArticleTagListResponse.builder()
                .articleId(articleId)
                .articleTitle(article.getTitle())
                .tags(allTags)
                .build();

        String message;
        if (addedTags.isEmpty() && !skippedTags.isEmpty()) {
            message = "Tất cả tags đã tồn tại trong bài viết, không có tag mới được thêm";
        } else if (!addedTags.isEmpty() && !skippedTags.isEmpty()) {
            message = "Đã thêm " + addedTags.size() + " tag(s). " + skippedTags.size()
                    + " tag(s) đã tồn tại được bỏ qua";
        } else {
            message = "Đã thêm " + addedTags.size() + " tag(s) vào bài viết thành công";
        }

        return ApiResponse.success(response, message);
    }

    @Override
    @Transactional
    public ApiResponse<String> removeTagFromArticle(Long articleId, String tag) {
        if (articleId == null || articleId <= 0) {
            throw new HttpBadRequest("ID bài viết không hợp lệ");
        }

        if (tag == null || tag.trim().isEmpty()) {
            throw new HttpBadRequest("Tên tag không được để trống");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết với id: " + articleId));

        List<ArticleTag> existingTags = articleTagRepository.findAllByArticleId(articleId)
                .stream()
                .filter(at -> at.getTag().equalsIgnoreCase(tag.trim()))
                .collect(Collectors.toList());

        if (existingTags.isEmpty()) {
            throw new HttpNotFound("Không tìm thấy tag '" + tag + "' trong bài viết '" + article.getTitle() + "'");
        }

        articleTagRepository.deleteAll(existingTags);
        return ApiResponse.success(null,
                "Đã xóa tag '" + tag + "' khỏi bài viết '" + article.getTitle() + "' thành công");
    }

    @Override
    public ApiResponse<List<PopularTagResponse>> getPopularTags(int limit) {
        if (limit <= 0) {
            throw new HttpBadRequest("Số lượng tags phải lớn hơn 0");
        }
        if (limit > 100) {
            throw new HttpBadRequest("Số lượng tags tối đa là 100");
        }

        List<Object[]> popularTags = articleTagRepository.findPopularTags(PageRequest.of(0, limit));

        if (popularTags.isEmpty()) {
            throw new HttpNotFound("Chưa có tag nào trong hệ thống");
        }

        List<PopularTagResponse> result = popularTags.stream()
                .map(row -> PopularTagResponse.builder()
                        .tag((String) row[0])
                        .articleCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(result, "Lấy danh sách " + result.size() + " tags phổ biến thành công");
    }

    @Override
    public ApiResponse<List<String>> getAllTags() {
        List<String> allTags = articleTagRepository.findDistinctTags();

        if (allTags.isEmpty()) {
            throw new HttpNotFound("Chưa có tag nào trong hệ thống");
        }

        return ApiResponse.success(allTags, "Tìm thấy " + allTags.size() + " tags trong hệ thống");
    }

    @Override
    public ApiResponse<ArticleTagListResponse> getTagsByArticleId(Long articleId) {
        if (articleId == null || articleId <= 0) {
            throw new HttpBadRequest("ID bài viết không hợp lệ");
        }

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết với id: " + articleId));

        List<String> tags = articleTagRepository.findAllByArticleId(articleId)
                .stream()
                .map(ArticleTag::getTag)
                .collect(Collectors.toList());

        ArticleTagListResponse response = ArticleTagListResponse.builder()
                .articleId(articleId)
                .articleTitle(article.getTitle())
                .tags(tags)
                .build();

        String message;
        if (tags.isEmpty()) {
            message = "Bài viết '" + article.getTitle() + "' chưa có tag nào";
        } else {
            message = "Tìm thấy " + tags.size() + " tag(s) trong bài viết '" + article.getTitle() + "'";
        }

        return ApiResponse.success(response, message);
    }

    @Override
    public ApiResponse<List<TagResponse>> searchTags(String keyword, Long articleId, Long categoryId,
            LocalDateTime createdFrom, LocalDateTime createdTo,
            int page, int size, String sortBy, String direction) {

        if (page < 0) {
            throw new HttpBadRequest("Số trang phải >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new HttpBadRequest("Số lượng mỗi trang phải từ 1 đến 100");
        }

        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new HttpBadRequest("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }

        if (articleId != null && articleId > 0) {
            if (!articleRepository.existsById(articleId)) {
                throw new HttpNotFound("Không tìm thấy bài viết với id: " + articleId);
            }
        }

        if (categoryId != null && categoryId > 0) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new HttpNotFound("Không tìm thấy danh mục với id: " + categoryId);
            }
        }

        List<String> validSortFields = List.of("id", "tag", "createdAt");
        if (!validSortFields.contains(sortBy)) {
            throw new HttpBadRequest("Trường sắp xếp không hợp lệ. Chấp nhận: " + String.join(", ", validSortFields));
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            throw new HttpBadRequest("Hướng sắp xếp không hợp lệ. Chấp nhận: ASC, DESC");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<ArticleTag> tagPage = articleTagRepository.searchWithFilters(
                keyword, articleId, categoryId, createdFrom, createdTo, pageRequest);

        if (tagPage.isEmpty()) {
            String filterInfo = buildFilterInfo(keyword, articleId, categoryId, createdFrom, createdTo);
            throw new HttpNotFound("Không tìm thấy tag nào" + filterInfo);
        }

        List<TagResponse> result = tagPage.getContent().stream()
                .map(at -> TagResponse.builder()
                        .id(at.getId())
                        .tag(at.getTag())
                        .articleId(at.getArticle().getId())
                        .articleTitle(at.getArticle().getTitle())
                        .createdAt(at.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        String message = "Tìm thấy " + tagPage.getTotalElements() + " tag(s). Trang " + (page + 1) + "/"
                + tagPage.getTotalPages();

        Pagination pagination = Pagination.builder()
                .currentPage(tagPage.getNumber())
                .pageSize(tagPage.getSize())
                .totalElements(tagPage.getTotalElements())
                .totalPages(tagPage.getTotalPages())
                .build();

        return ApiResponse.success(result, message, pagination);
    }

    private String buildFilterInfo(String keyword, Long articleId, Long categoryId,
            LocalDateTime createdFrom, LocalDateTime createdTo) {
        StringBuilder sb = new StringBuilder();
        List<String> filters = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            filters.add("từ khóa '" + keyword + "'");
        }
        if (articleId != null) {
            filters.add("bài viết id=" + articleId);
        }
        if (categoryId != null) {
            filters.add("danh mục id=" + categoryId);
        }
        if (createdFrom != null || createdTo != null) {
            filters.add("khoảng thời gian");
        }

        if (!filters.isEmpty()) {
            sb.append(" với bộ lọc: ").append(String.join(", ", filters));
        }

        return sb.toString();
    }
}
