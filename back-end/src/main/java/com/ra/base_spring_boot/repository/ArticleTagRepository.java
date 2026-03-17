package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.ArticleTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {

        List<ArticleTag> findAllByArticleId(Long articleId);

        void deleteAllByArticleId(Long articleId);

        @Query("SELECT DISTINCT at.tag FROM ArticleTag at")
        List<String> findDistinctTags();

        @Query("SELECT at.tag, COUNT(at) as cnt FROM ArticleTag at GROUP BY at.tag ORDER BY cnt DESC")
        List<Object[]> findPopularTags(Pageable pageable);

        @Query("SELECT at.article.id FROM ArticleTag at WHERE LOWER(at.tag) = LOWER(:tag)")
        List<Long> findArticleIdsByTag(@Param("tag") String tag);

        boolean existsByArticleIdAndTagIgnoreCase(Long articleId, String tag);

        @Query("SELECT at FROM ArticleTag at WHERE " +
                        "(:keyword IS NULL OR LOWER(at.tag) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:articleId IS NULL OR at.article.id = :articleId) AND " +
                        "(:categoryId IS NULL OR at.article.category.id = :categoryId) AND " +
                        "(:createdFrom IS NULL OR at.createdAt >= :createdFrom) AND " +
                        "(:createdTo IS NULL OR at.createdAt <= :createdTo)")
        Page<ArticleTag> searchWithFilters(
                        @Param("keyword") String keyword,
                        @Param("articleId") Long articleId,
                        @Param("categoryId") Long categoryId,
                        @Param("createdFrom") LocalDateTime createdFrom,
                        @Param("createdTo") LocalDateTime createdTo,
                        Pageable pageable);
}
