package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    void deleteAllByArticleId(Long articleId);

    List<Approval> findAllByArticleIdOrderByCreatedAtDesc(Long articleId);

    List<Approval> findAllByArticleIdOrderByCreatedAtAsc(Long articleId);
}
