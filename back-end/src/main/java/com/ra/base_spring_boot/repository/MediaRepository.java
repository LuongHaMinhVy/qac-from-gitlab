package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
        List<Media> findByIsDeletedFalse();

        Optional<Media> findByOriginalNameAndFileSizeAndIsDeletedFalse(String originalName, Long fileSize);

        @Query("SELECT m FROM Media m WHERE " +
                        "(m.isDeleted = false) AND " +
                        "(:keyword IS NULL OR " +
                        "LOWER(m.fileName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(m.originalName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(m.altText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(m.caption) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:mimeType IS NULL OR m.mimeType LIKE CONCAT('%', :mimeType, '%')) AND " +
                        "(:createdFrom IS NULL OR m.createdAt >= :createdFrom) AND " +
                        "(:createdTo IS NULL OR m.createdAt <= :createdTo)")
        Page<Media> findAllWithFilter(
                        @Param("keyword") String keyword,
                        @Param("mimeType") String mimeType,
                        @Param("createdFrom") LocalDateTime createdFrom,
                        @Param("createdTo") LocalDateTime createdTo,
                        Pageable pageable);
}
