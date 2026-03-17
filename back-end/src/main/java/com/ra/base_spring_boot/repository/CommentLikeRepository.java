package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.article.id = :articleId")
    void deleteAllByCommentArticleId(@Param("articleId") Long articleId);
}
