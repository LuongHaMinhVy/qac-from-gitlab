package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.ReferenceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ArticleReferences")
@Builder
public class ArticleReference extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "ArticleID", nullable = false)
    private Article article;

    @Column(name = "Citation", nullable = false, columnDefinition = "TEXT")
    private String citation;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", length = 20)
    private ReferenceType type;

    @Column(name = "URL", length = 500)
    private String url;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
