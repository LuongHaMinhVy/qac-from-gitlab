package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Article;
import com.ra.base_spring_boot.model.constants.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

        Optional<Article> findBySlug(String slug);

        Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);

        long countByStatus(ArticleStatus status);

        long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        @Query("SELECT SUM(a.viewCount) FROM Article a")
        Long sumViewCount();

        @Query("SELECT FUNCTION('MONTH', a.createdAt) as month, COUNT(a) as count " +
                        "FROM Article a WHERE a.createdAt >= :startDate " +
                        "GROUP BY FUNCTION('MONTH', a.createdAt) " +
                        "ORDER BY FUNCTION('MONTH', a.createdAt)")
        List<Object[]> countArticlesByMonth(@Param("startDate") LocalDateTime startDate);

        @Query("SELECT a FROM Article a WHERE " +
                        "(:status IS NULL OR a.status = :status) AND " +
                        "(:search IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
                        "(:isHighlight IS NULL OR a.isHighlight = :isHighlight) AND " +
                        "(:isFeatured IS NULL OR a.isFeatured = :isFeatured) AND " +
                        "(:hashtag IS NULL OR LOWER(a.hashtag) = LOWER(:hashtag))")
        Page<Article> findAllWithFilter(@Param("search") String search,
                        @Param("status") ArticleStatus status,
                        @Param("categoryId") Long categoryId,
                        @Param("isHighlight") Boolean isHighlight,
                        @Param("isFeatured") Boolean isFeatured,
                        @Param("hashtag") String hashtag,
                        Pageable pageable);

        @Query("select a.id from Article a where a.category.id = :categoryId")
        List<Long> findIdsByCategoryId(@Param("categoryId") Long categoryId);

        @Modifying
        @Query("delete from Article a where a.category.id = :categoryId")
        int deleteAllByCategoryId(@Param("categoryId") Long categoryId);

        @Modifying
        @Query("update Article a set a.category = null where a.category.id = :categoryId")
        int clearCategoryByCategoryId(@Param("categoryId") Long categoryId);

        @Query("SELECT a FROM Article a WHERE a.id IN :ids AND " +
                        "(:status IS NULL OR a.status = :status) AND " +
                        "(:search IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
                        "(:isHighlight IS NULL OR a.isHighlight = :isHighlight) AND " +
                        "(:isFeatured IS NULL OR a.isFeatured = :isFeatured)")
        Page<Article> findAllByIdInWithFilter(@Param("ids") List<Long> ids,
                        @Param("search") String search,
                        @Param("status") ArticleStatus status,
                        @Param("categoryId") Long categoryId,
                        @Param("isHighlight") Boolean isHighlight,
                        @Param("isFeatured") Boolean isFeatured,
                        Pageable pageable);

        @Query("SELECT a FROM Article a WHERE " +
                        "(:search IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
                        " OR LOWER(CAST(a.content AS string)) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:status IS NULL OR a.status = :status) AND " +
                        "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
                        "(:authorId IS NULL OR a.author.accountId = :authorId) AND " +
                        "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
                        "(:endDate IS NULL OR a.createdAt <= :endDate) AND " +
                        "(:isHighlight IS NULL OR a.isHighlight = :isHighlight) AND " +
                        "(:isFeatured IS NULL OR a.isFeatured = :isFeatured)")
        Page<Article> advancedSearch(@Param("search") String search,
                        @Param("status") ArticleStatus status,
                        @Param("categoryId") Long categoryId,
                        @Param("authorId") Integer authorId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("isHighlight") Boolean isHighlight,
                        @Param("isFeatured") Boolean isFeatured,
                        Pageable pageable);

        List<Article> findTop5ByHashtagAndStatusAndIdNotOrderByPublishedAtDesc(String hashtag, ArticleStatus status,
                        Long id);
}
