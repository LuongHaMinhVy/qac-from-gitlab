package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.RelatedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatedArticleRepository extends JpaRepository<RelatedArticle, Long> {

    List<RelatedArticle> findAllBySourceArticleId(Long sourceArticleId);

    void deleteAllBySourceArticleId(Long sourceArticleId);

    void deleteAllByRelatedArticleId(Long relatedArticleId);

    boolean existsBySourceArticleIdAndRelatedArticleId(Long sourceArticleId, Long relatedArticleId);
}
