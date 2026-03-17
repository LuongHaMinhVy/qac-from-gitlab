package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Article;
import com.ra.base_spring_boot.model.UserInteraction;
import com.ra.base_spring_boot.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    void deleteAllByArticleId(Long articleId);

    void deleteAllByVideoId(Long videoId);

    Optional<UserInteraction> findByAccountAndArticle(Account account, Article article);

    Optional<UserInteraction> findByAccountAndVideo(Account account, Video video);
}
