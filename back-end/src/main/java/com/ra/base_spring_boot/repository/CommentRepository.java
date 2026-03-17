package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    void deleteAllByArticleId(Long articleId);

    void deleteAllByVideoId(Long videoId);

    Page<Comment> findAllByArticleId(Long articleId, Pageable pageable);

    Page<Comment> findAllByVideoId(Long videoId, Pageable pageable);

    @Modifying
    @Query("UPDATE Comment c SET c.parent = null WHERE c.article.id = :articleId")
    void clearParentByArticleId(@Param("articleId") Long articleId);

    @Modifying
    @Query("UPDATE Comment c SET c.parent = null WHERE c.video.id = :videoId")
    void clearParentByVideoId(@Param("videoId") Long videoId);
}
