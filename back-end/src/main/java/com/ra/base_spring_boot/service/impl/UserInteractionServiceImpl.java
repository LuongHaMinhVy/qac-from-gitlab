package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.req.UserInteractionRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.model.constants.InteractionType;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpForbiden;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Article;
import com.ra.base_spring_boot.model.UserInteraction;
import com.ra.base_spring_boot.model.Video;
import com.ra.base_spring_boot.repository.ArticleRepository;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.UserInteractionRepository;
import com.ra.base_spring_boot.repository.VideoRepository;
import com.ra.base_spring_boot.service.UserInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserInteractionServiceImpl implements UserInteractionService {

    private final UserInteractionRepository userInteractionRepository;
    private final ArticleRepository articleRepository;
    private final VideoRepository videoRepository;
    private final IAccountRepository accountRepository;

    @Override
    @Transactional
    public ApiResponse<String> interact(UserInteractionRequest request) {
        if (request.getArticleId() == null && request.getVideoId() == null) {
            throw new HttpBadRequest("Tương tác phải thuộc về bài viết hoặc video");
        }
        if (request.getArticleId() != null && request.getVideoId() != null) {
            throw new HttpBadRequest("Tương tác chỉ được thuộc về một đối tượng");
        }

        Account author = getCurrentAccount();
        Article article = null;
        Video video = null;
        UserInteraction existingInteraction = null;

        if (request.getArticleId() != null) {
            article = articleRepository.findById(request.getArticleId())
                    .orElseThrow(() -> new HttpNotFound("Bài viết không tồn tại"));
            existingInteraction = userInteractionRepository.findByAccountAndArticle(author, article).orElse(null);
        } else {
            video = videoRepository.findById(request.getVideoId())
                    .orElseThrow(() -> new HttpNotFound("Video không tồn tại"));
            existingInteraction = userInteractionRepository.findByAccountAndVideo(author, video).orElse(null);
        }

        if (existingInteraction != null) {
            if (existingInteraction.getInteractionType() == request.getInteractionType()) {
                userInteractionRepository.delete(existingInteraction);

                if (article != null) {
                    if (article.getLikeCount() > 0) {
                        article.setLikeCount(article.getLikeCount() - 1);
                        articleRepository.save(article);
                    }
                } else {
                    if (video.getLikeCount() > 0) {
                        video.setLikeCount(video.getLikeCount() - 1);
                        videoRepository.save(video);
                    }
                }
                return ApiResponse.success(null, "Đã bỏ tương tác");
            } else {
                InteractionType oldType = existingInteraction.getInteractionType();
                InteractionType newType = request.getInteractionType();

                existingInteraction.setInteractionType(newType);
                existingInteraction.setUpdatedAt(LocalDateTime.now());
                userInteractionRepository.save(existingInteraction);

                if (oldType == InteractionType.like && newType != InteractionType.like) {
                    if (article != null) {
                        if (article.getLikeCount() > 0) {
                            article.setLikeCount(article.getLikeCount() - 1);
                            articleRepository.save(article);
                        }
                    } else {
                        if (video.getLikeCount() > 0) {
                            video.setLikeCount(video.getLikeCount() - 1);
                            videoRepository.save(video);
                        }
                    }
                } else if (oldType != InteractionType.like && newType == InteractionType.like) {
                    if (article != null) {
                        if (article.getLikeCount() == null)
                            article.setLikeCount(0);
                        article.setLikeCount(article.getLikeCount() + 1);
                        articleRepository.save(article);
                    } else {
                        if (video.getLikeCount() == null)
                            video.setLikeCount(0);
                        video.setLikeCount(video.getLikeCount() + 1);
                        videoRepository.save(video);
                    }
                }
                return ApiResponse.success(null, "Đã cập nhật tương tác");
            }
        } else {
            UserInteraction interaction = UserInteraction.builder()
                    .account(author)
                    .article(article)
                    .video(video)
                    .interactionType(request.getInteractionType())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userInteractionRepository.save(interaction);

            if (request.getInteractionType() == InteractionType.like) {
                if (article != null) {
                    if (article.getLikeCount() == null)
                        article.setLikeCount(0);
                    article.setLikeCount(article.getLikeCount() + 1);
                    articleRepository.save(article);
                } else {
                    if (video.getLikeCount() == null)
                        video.setLikeCount(0);
                    video.setLikeCount(video.getLikeCount() + 1);
                    videoRepository.save(video);
                }
            }
            return ApiResponse.success(null, "Đã tương tác thành công");
        }
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
}
