package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.ArticleRejectRequest;
import com.ra.base_spring_boot.dto.req.ArticleRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.ApprovalResponse;
import com.ra.base_spring_boot.dto.resp.ArticleResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Article;
import com.ra.base_spring_boot.model.Category;
import com.ra.base_spring_boot.model.constants.ArticleStatus;
import com.ra.base_spring_boot.repository.ArticleRepository;
import com.ra.base_spring_boot.repository.CategoryRepository;
import com.ra.base_spring_boot.repository.CommentLikeRepository;
import com.ra.base_spring_boot.repository.UserInteractionRepository;
import com.ra.base_spring_boot.repository.CommentRepository;
import com.ra.base_spring_boot.repository.ApprovalRepository;
import com.ra.base_spring_boot.service.ArticleService;
import com.ra.base_spring_boot.service.NotificationService;
import com.ra.base_spring_boot.service.ContentModerationService;
import com.ra.base_spring_boot.service.MediaService;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Approval;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.text.Normalizer;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

        private final ArticleRepository articleRepository;
        private final CategoryRepository categoryRepository;
        private final CommentLikeRepository commentLikeRepository;
        private final IAccountRepository accountRepository;
        private final NotificationService notificationService;
        private final ApprovalRepository approvalRepository;
        private final UserInteractionRepository userInteractionRepository;
        private final CommentRepository commentRepository;
        private final ContentModerationService contentModerationService;
        private final MediaService mediaService;

        private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
        private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

        @Override
        public ApiResponse<List<ArticleResponse>> getAllArticles(String search, ArticleStatus status, Long categoryId,
                        Boolean isHighlight, Boolean isFeatured, String hashtag,
                        int page, int size, String sort, String direction) {
                Sort.Direction sortDirection = Sort.Direction.fromString(direction);
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

                Page<Article> articles = articleRepository.findAllWithFilter(search, status, categoryId, isHighlight,
                                isFeatured, hashtag, pageable);

                List<ArticleResponse> responseList = articles.map(this::mapToResponseBasic).getContent();

                Pagination pagination = com.ra.base_spring_boot.dto.resp.Pagination.builder()
                                .currentPage(articles.getNumber())
                                .pageSize(articles.getSize())
                                .totalElements(articles.getTotalElements())
                                .totalPages(articles.getTotalPages())
                                .build();

                ApiResponse<List<ArticleResponse>> response = ApiResponse.success(responseList,
                                "Lấy danh sách bài viết thành công");
                response.setPagination(pagination);
                return response;
        }

        @Override
        public ApiResponse<List<ArticleResponse>> advancedSearch(String search, ArticleStatus status, Long categoryId,
                        Integer authorId, LocalDateTime startDate, LocalDateTime endDate,
                        Boolean isHighlight, Boolean isFeatured, int page, int size, String sort, String direction) {
                Sort.Direction sortDirection = Sort.Direction.fromString(direction);
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

                Page<Article> articles = articleRepository.advancedSearch(search, status, categoryId, authorId,
                                startDate, endDate, isHighlight, isFeatured, pageable);

                List<ArticleResponse> responseList = articles.map(this::mapToResponseBasic).getContent();

                Pagination pagination = com.ra.base_spring_boot.dto.resp.Pagination.builder()
                                .currentPage(articles.getNumber())
                                .pageSize(articles.getSize())
                                .totalElements(articles.getTotalElements())
                                .totalPages(articles.getTotalPages())
                                .build();

                ApiResponse<List<ArticleResponse>> response = ApiResponse.success(responseList,
                                "Tìm kiếm nâng cao thành công");
                response.setPagination(pagination);
                return response;
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> getArticleById(Long id) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                article.setViewCount(article.getViewCount() + 1);
                articleRepository.save(article);

                return ApiResponse.success(mapToResponse(article), "Lấy chi tiết bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> getArticleBySlug(String slug) {
                Article article = articleRepository.findBySlug(slug)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                article.setViewCount(article.getViewCount() + 1);
                articleRepository.save(article);

                return ApiResponse.success(mapToResponse(article), "Lấy chi tiết bài viết thành công");
        }

        private Account getCurrentAccount() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                return accountRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> createArticle(ArticleRequest request,
                        MultipartFile file) {
                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy danh mục"));

                Account author = getCurrentAccount();

                contentModerationService.validateContent(request.getTitle());
                contentModerationService.validateContent(request.getExcerpt());
                contentModerationService.validateContent(request.getContent());

                ArticleStatus initialStatus = ArticleStatus.draft;

                Article article = Article.builder()
                                .title(contentModerationService.sanitizeContent(request.getTitle()))
                                .slug(toSlug(request.getTitle()))
                                .excerpt(contentModerationService.sanitizeContent(request.getExcerpt()))
                                .content(contentModerationService.sanitizeContent(request.getContent()))
                                .category(category)
                                .author(author)
                                .featuredImage(file != null && !file.isEmpty()
                                                ? mediaService.uploadFile(file)
                                                : (request.getMediaId() != null
                                                                ? mediaService.findById(request.getMediaId())
                                                                : null))
                                .status(initialStatus)
                                .isHighlight(request.getIsHighlight() != null ? request.getIsHighlight() : false)
                                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                                .allowComments(request.getAllowComments() != null ? request.getAllowComments() : true)
                                .hashtag(normalizeHashtag(request.getHashtag()))
                                .viewCount(0)
                                .likeCount(0)
                                .commentCount(0)
                                .publishedAt(null)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                Article savedArticle = articleRepository.save(article);

                return ApiResponse.success(mapToResponse(savedArticle),
                                "Tạo bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> updateArticle(Long id, ArticleRequest request,
                        MultipartFile file) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy danh mục"));

                checkPrivilegeAndOwnership(article);

                if (article.getStatus() == ArticleStatus.rejected
                                || article.getStatus() == ArticleStatus.needs_revision) {
                        article.setStatus(ArticleStatus.draft);
                }

                contentModerationService.validateContent(request.getTitle());
                contentModerationService.validateContent(request.getExcerpt());
                contentModerationService.validateContent(request.getContent());

                article.setTitle(contentModerationService.sanitizeContent(request.getTitle()));
                article.setSlug(toSlug(request.getTitle()));
                article.setExcerpt(contentModerationService.sanitizeContent(request.getExcerpt()));
                article.setContent(contentModerationService.sanitizeContent(request.getContent()));
                article.setCategory(category);
                if (file != null && !file.isEmpty()) {
                        article.setFeaturedImage(mediaService.uploadFile(file));
                } else if (request.getMediaId() != null) {
                        article.setFeaturedImage(mediaService.findById(request.getMediaId()));
                }

                article.setHashtag(normalizeHashtag(request.getHashtag()));

                if (request.getIsHighlight() != null)
                        article.setIsHighlight(request.getIsHighlight());
                if (request.getIsFeatured() != null)
                        article.setIsFeatured(request.getIsFeatured());
                if (request.getAllowComments() != null)
                        article.setAllowComments(request.getAllowComments());
                article.setUpdatedAt(LocalDateTime.now());

                Article savedArticle = articleRepository.save(article);

                return ApiResponse.success(mapToResponse(savedArticle),
                                "Cập nhật bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<String> deleteArticle(Long id) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                approvalRepository.deleteAllByArticleId(id);
                userInteractionRepository.deleteAllByArticleId(id);

                commentLikeRepository.deleteAllByCommentArticleId(id);
                commentRepository.clearParentByArticleId(id);
                commentRepository.deleteAllByArticleId(id);

                articleRepository.delete(article);
                return ApiResponse.success(null, "Xóa bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> submitForReview(Long id) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                checkPrivilegeAndOwnership(article);

                if (article.getStatus() != ArticleStatus.draft) {
                        throw new HttpBadRequest(
                                        "Chỉ bài viết ở trạng thái nháp mới có thể gửi duyệt. Vui lòng cập nhật bài viết để đưa về trạng thái nháp trước khi gửi duyệt lại.");
                }

                ArticleStatus oldStatus = article.getStatus();
                article.setStatus(ArticleStatus.pending_review);
                article.setUpdatedAt(LocalDateTime.now());
                Article savedArticle = articleRepository.save(article);

                saveReviewLog(savedArticle, oldStatus, ArticleStatus.pending_review, "Gửi duyệt bài viết");

                notificationService.notifyArticleStatusChange(article.getAuthor(), article.getTitle(), "Chờ duyệt");

                return ApiResponse.success(mapToResponse(savedArticle), "Gửi duyệt bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> approveArticle(Long id) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                if (article.getStatus() != ArticleStatus.pending_review) {
                        throw new HttpBadRequest("Chỉ bài viết đang chờ duyệt mới có thể phê duyệt");
                }

                ArticleStatus oldStatus = article.getStatus();
                article.setStatus(ArticleStatus.approved);
                article.setUpdatedAt(LocalDateTime.now());
                Article savedArticle = articleRepository.save(article);

                saveReviewLog(savedArticle, oldStatus, ArticleStatus.approved, "Phê duyệt bài viết");

                notificationService.notifyArticleStatusChange(article.getAuthor(), article.getTitle(), "Đã phê duyệt");

                return ApiResponse.success(mapToResponse(savedArticle), "Phê duyệt bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> rejectArticle(Long id,
                        ArticleRejectRequest request) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                if (article.getStatus() != ArticleStatus.pending_review
                                && article.getStatus() != ArticleStatus.approved) {
                        throw new HttpBadRequest("Chỉ bài viết đang chờ duyệt hoặc đã phê duyệt mới có thể từ chối");
                }

                ArticleStatus oldStatus = article.getStatus();
                article.setStatus(ArticleStatus.rejected);
                article.setUpdatedAt(LocalDateTime.now());
                Article savedArticle = articleRepository.save(article);

                saveReviewLog(savedArticle, oldStatus, ArticleStatus.rejected, request.getReason());

                notificationService.notifyArticleStatusChange(article.getAuthor(), article.getTitle(),
                                "Bị từ chối: " + request.getReason());

                return ApiResponse.success(mapToResponse(savedArticle), "Từ chối bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> publishArticle(Long id) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                if (article.getStatus() != ArticleStatus.approved) {
                        throw new HttpBadRequest("Chỉ bài viết đã phê duyệt mới có thể công khai");
                }

                ArticleStatus oldStatus = article.getStatus();
                article.setStatus(ArticleStatus.published);
                article.setPublishedAt(LocalDateTime.now());
                article.setUpdatedAt(LocalDateTime.now());
                Article savedArticle = articleRepository.save(article);

                saveReviewLog(savedArticle, oldStatus, ArticleStatus.published, "Công khai bài viết");

                notificationService.notifyArticleStatusChange(article.getAuthor(), article.getTitle(), "Đã công khai");

                return ApiResponse.success(mapToResponse(savedArticle), "Công khai bài viết thành công");
        }

        @Override
        @Transactional
        public ApiResponse<ArticleResponse> requestRevision(Long id,
                        ArticleRejectRequest request) {
                Article article = articleRepository.findById(id)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết"));

                if (article.getStatus() != ArticleStatus.pending_review) {
                        throw new HttpBadRequest("Chỉ bài viết đang chờ duyệt mới có thể yêu cầu sửa đổi");
                }

                ArticleStatus oldStatus = article.getStatus();
                article.setStatus(ArticleStatus.needs_revision);
                article.setUpdatedAt(LocalDateTime.now());
                Article savedArticle = articleRepository.save(article);

                saveReviewLog(savedArticle, oldStatus, ArticleStatus.needs_revision, request.getReason());

                notificationService.notifyArticleStatusChange(article.getAuthor(), article.getTitle(),
                                "Yêu cầu sửa đổi: " + request.getReason());

                return ApiResponse.success(mapToResponse(savedArticle), "Yêu cầu sửa đổi bài viết gửi thành công");
        }

        @Override
        public ApiResponse<List<ApprovalResponse>> getReviewLogs(Long articleId) {
                Article article = articleRepository.findById(articleId)
                                .orElseThrow(() -> new HttpNotFound("Không tìm thấy bài viết với ID: " + articleId));

                List<Approval> approvalLogs = approvalRepository.findAllByArticleIdOrderByCreatedAtAsc(articleId);

                List<ApprovalResponse> logs = approvalLogs.stream()
                                .map(log -> ApprovalResponse.builder()
                                                .id(log.getId())
                                                .articleId(log.getArticle().getId())
                                                .articleTitle(log.getArticle().getTitle())
                                                .reviewerId(log.getReviewer().getAccountId())
                                                .reviewerName(log.getReviewer().getUsername())
                                                .oldStatus(log.getOldStatus())
                                                .newStatus(log.getNewStatus())
                                                .reason(log.getReason())
                                                .createdAt(log.getCreatedAt())
                                                .build())
                                .collect(Collectors.toList());

                if (logs.isEmpty()) {
                        logs.add(ApprovalResponse.builder()
                                        .articleId(article.getId())
                                        .articleTitle(article.getTitle())
                                        .reviewerId(article.getAuthor().getAccountId())
                                        .reviewerName(article.getAuthor().getUsername())
                                        .oldStatus(null)
                                        .newStatus(ArticleStatus.draft)
                                        .reason("Bài viết được tạo (Nháp)")
                                        .createdAt(article.getCreatedAt())
                                        .build());
                }

                return ApiResponse.success(logs, "Lấy lịch sử phê duyệt thành công");
        }

        private void saveReviewLog(Article article, ArticleStatus oldStatus, ArticleStatus newStatus, String reason) {
                Account reviewer = getCurrentAccount();
                Approval log = Approval
                                .builder()
                                .article(article)
                                .reviewer(reviewer)
                                .oldStatus(oldStatus)
                                .newStatus(newStatus)
                                .reason(reason)
                                .createdAt(LocalDateTime.now())
                                .build();
                approvalRepository.save(log);
        }

        private void checkPrivilegeAndOwnership(Article article) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean hasUpdateAll = auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(PermissionCode.ARTICLE_UPDATE_ALL));

                if (hasUpdateAll) {
                        return;
                }

                Account currentAccount = getCurrentAccount();
                if (!article.getAuthor().getAccountId().equals(currentAccount.getAccountId())) {
                        throw new HttpForbiden("Chỉ tác giả mới có quyền chỉnh sửa bài viết này");
                }
        }

        private boolean hasRole(String roleName) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                return auth.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals(roleName));
        }

        private ArticleResponse mapToResponse(Article article) {
                ArticleResponse response = mapToResponseBasic(article);

                if (article.getStatus() == ArticleStatus.published && article.getHashtag() != null
                                && !article.getHashtag().isEmpty()) {
                        List<Article> related = articleRepository
                                        .findTop5ByHashtagAndStatusAndIdNotOrderByPublishedAtDesc(
                                                        article.getHashtag(), ArticleStatus.published, article.getId());
                        response.setRelatedArticles(related.stream()
                                        .map(this::mapToResponseBasic)
                                        .collect(Collectors.toList()));
                }

                return response;
        }

        private ArticleResponse mapToResponseBasic(Article article) {
                return ArticleResponse.builder()
                                .id(article.getId())
                                .title(article.getTitle())
                                .slug(article.getSlug())
                                .excerpt(article.getExcerpt())
                                .content(article.getContent())
                                .featuredImage(article.getFeaturedImage() != null
                                                ? mediaService.convertToDTO(article.getFeaturedImage())
                                                : null)
                                .status(article.getStatus())
                                .isHighlight(article.getIsHighlight())
                                .isFeatured(article.getIsFeatured())
                                .allowComments(article.getAllowComments())
                                .viewCount(article.getViewCount())
                                .likeCount(article.getLikeCount())
                                .commentCount(article.getCommentCount())
                                .publishedAt(article.getPublishedAt())
                                .createdAt(article.getCreatedAt())
                                .updatedAt(article.getUpdatedAt())
                                .authorName(article.getAuthor() != null ? article.getAuthor().getUsername() : "Unknown")
                                .categoryName(article.getCategory().getName())
                                .categoryId(article.getCategory().getId())
                                .hashtag(article.getHashtag())
                                .build();
        }

        private String normalizeHashtag(String hashtag) {
                if (hashtag == null)
                        return null;
                return hashtag.trim().toLowerCase().replace("#", "");
        }

        private String toSlug(String input) {
                String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
                String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
                String slug = NONLATIN.matcher(normalized).replaceAll("");
                return slug.toLowerCase(Locale.ENGLISH);
        }
}
