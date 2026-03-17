package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.RelationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "RelatedArticles")
public class RelatedArticle extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "SourceArticleID", nullable = false)
    private Article sourceArticle;

    @ManyToOne
    @JoinColumn(name = "RelatedArticleID", nullable = false)
    private Article relatedArticle;

    @Enumerated(EnumType.STRING)
    @Column(name = "RelationType", length = 20)
    private RelationType relationType;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
