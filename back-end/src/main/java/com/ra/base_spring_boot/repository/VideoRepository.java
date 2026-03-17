package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Video;
import com.ra.base_spring_boot.model.constants.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findBySlug(String slug);

    Page<Video> findAllByTitleContainingIgnoreCase(String search, Pageable pageable);

    Page<Video> findAllByStatus(VideoStatus status, Pageable pageable);

    @Query("SELECT v FROM Video v WHERE (:status IS NULL OR v.status = :status) " +
            "AND (:categoryId IS NULL OR v.category.id = :categoryId) " +
            "AND (:title IS NULL OR v.title LIKE %:title%)")
    Page<Video> findAllWithFilters(VideoStatus status, Long categoryId, String title, Pageable pageable);
}
