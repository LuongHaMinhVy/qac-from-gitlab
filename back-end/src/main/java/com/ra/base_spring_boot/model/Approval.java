package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Approvals")
public class Approval extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "ArticleID", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "ReviewerID", nullable = false)
    private Account reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "OldStatus", length = 20)
    private ArticleStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "NewStatus", length = 20, nullable = false)
    private ArticleStatus newStatus;

    @Column(name = "Reason", columnDefinition = "TEXT")
    private String reason;

    @Builder.Default
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
