package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.CommentRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.CommentResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Article;
import com.ra.base_spring_boot.model.Comment;
import com.ra.base_spring_boot.model.Video;
import com.ra.base_spring_boot.model.constants.CommentStatus;
import com.ra.base_spring_boot.repository.ArticleRepository;
import com.ra.base_spring_boot.repository.CommentRepository;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.VideoRepository;
import com.ra.base_spring_boot.service.CommentService;
import com.ra.base_spring_boot.service.NotificationService;
import com.ra.base_spring_boot.service.ContentModerationService;
import com.ra.base_spring_boot.model.constants.PermissionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final VideoRepository videoRepository;
    private final IAccountRepository accountRepository;
    private final ContentModerationService contentModerationService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ApiResponse<CommentResponse> createComment(CommentRequest request) {
        if (request.getArticleId() == null && request.getVideoId() == null) {
            throw new HttpBadRequest("Bình luận phải thuộc về bài viết hoặc video");
        }
        if (request.getArticleId() != null && request.getVideoId() != null) {
            throw new HttpBadRequest("Bình luận chỉ được thuộc về một đối tượng (bài viết hoặc video)");
        }

        Account author = getCurrentAccount();

        contentModerationService.validateContent(request.getContent());

        Article article = null;
        Video video = null;

        if (request.getArticleId() != null) {
            article = articleRepository.findById(request.getArticleId())
                    .orElseThrow(() -> new HttpNotFound("Bài viết không tồn tại"));
        } else {
            video = videoRepository.findById(request.getVideoId())
                    .orElseThrow(() -> new HttpNotFound("Video không tồn tại"));
        }

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new HttpNotFound("Bình luận cha không tồn tại"));
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .article(article)
                .video(video)
                .parent(parent)
                .depth(parent != null ? parent.getDepth() + 1 : 0)
                .status(CommentStatus.approved)
                .likeCount(0)
                .isEdited(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        List<Account> mentionedAccounts = extractMentions(request.getContent());
        for (Account recipient : mentionedAccounts) {
            notificationService.createNotification(
                    recipient,
                    "Bạn được nhắc tới trong một bình luận",
                    author.getUsername() + " đã nhắc tới bạn trong một bình luận: " + request.getContent(),
                    com.ra.base_spring_boot.model.constants.NotificationType.system);
        }

        if (article != null) {
            if (article.getCommentCount() == null)
                article.setCommentCount(0);
            article.setCommentCount(article.getCommentCount() + 1);
            articleRepository.save(article);
        } else if (video != null) {
            if (video.getCommentCount() == null)
                video.setCommentCount(0);
            video.setCommentCount(video.getCommentCount() + 1);
            videoRepository.save(video);
        }

        return ApiResponse.success(mapToResponse(savedComment), "Bình luận thành công");
    }

    @Override
    @Transactional
    public ApiResponse<CommentResponse> updateComment(Long id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Bình luận không tồn tại"));

        Account currentAccount = getCurrentAccount();
        if (!comment.getAuthor().getAccountId().equals(currentAccount.getAccountId())) {
            throw new HttpForbiden("Bạn không có quyền sửa bình luận này");
        }

        contentModerationService.validateContent(request.getContent());

        comment.setContent(request.getContent());
        comment.setIsEdited(true);
        comment.setUpdatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return ApiResponse.success(mapToResponse(savedComment), "Cập nhật bình luận thành công");
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Bình luận không tồn tại"));

        Account currentAccount = getCurrentAccount();
        boolean isPrivileged = hasRole(PermissionCode.COMMENT_DELETE_ALL);
        if (!isPrivileged && !comment.getAuthor().getAccountId().equals(currentAccount.getAccountId())) {
            throw new HttpForbiden("Bạn không có quyền xóa bình luận này");
        }

        if (comment.getArticle() != null) {
            Article article = comment.getArticle();
            if (article.getCommentCount() > 0) {
                article.setCommentCount(article.getCommentCount() - 1);
                articleRepository.save(article);
            }
        } else if (comment.getVideo() != null) {
            Video video = comment.getVideo();
            if (video.getCommentCount() > 0) {
                video.setCommentCount(video.getCommentCount() - 1);
                videoRepository.save(video);
            }
        }

        commentRepository.delete(comment);
        return ApiResponse.success(null, "Xóa bình luận thành công");
    }

    @Override
    public ApiResponse<List<CommentResponse>> getCommentsByArticleId(Long articleId, int page, int size,
            String sort,
            String direction) {
        Sort sorting = Sort.by(direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Comment> comments = commentRepository.findAllByArticleId(articleId, pageable);
        List<CommentResponse> responseList = comments.map(this::mapToResponse).getContent();

        Pagination pagination = Pagination.builder()
                .currentPage(comments.getNumber())
                .pageSize(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();

        return ApiResponse.success(responseList, "Lấy danh sách bình luận thành công", pagination);
    }

    @Override
    public ApiResponse<List<CommentResponse>> getCommentsByVideoId(Long videoId, int page, int size,
            String sort,
            String direction) {
        Sort sorting = Sort.by(direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Comment> comments = commentRepository.findAllByVideoId(videoId, pageable);
        List<CommentResponse> responseList = comments.map(this::mapToResponse).getContent();

        Pagination pagination = Pagination.builder()
                .currentPage(comments.getNumber())
                .pageSize(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();

        return ApiResponse.success(responseList, "Lấy danh sách bình luận thành công", pagination);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(Long.valueOf(comment.getAuthor().getAccountId()))
                .authorName(comment.getAuthor().getUsername())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .depth(comment.getDepth())
                .likeCount(comment.getLikeCount())
                .isEdited(comment.getIsEdited())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .mentionedUsers(extractMentionedUsernames(comment.getContent()))
                .build();
    }

    private List<Account> extractMentions(String content) {
        if (content == null || !content.contains("@"))
            return new ArrayList<>();

        List<Account> recipients = new ArrayList<>();
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String username = matcher.group(1);
            accountRepository.findByUsername(username).ifPresent(recipients::add);
        }
        return recipients;
    }

    private List<String> extractMentionedUsernames(String content) {
        if (content == null || !content.contains("@"))
            return new ArrayList<>();

        List<String> usernames = new ArrayList<>();
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames.stream().distinct().collect(Collectors.toList());
    }

    private Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new HttpForbiden("Bạn cần đăng nhập để thực hiện thao tác này");
        }
        String username = authentication.getName();
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFound("Tài khoản không tồn tại"));
    }

    private boolean hasRole(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
