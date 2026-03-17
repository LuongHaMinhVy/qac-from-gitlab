package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "ArticleTags")
@Builder
public class ArticleTag extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "ArticleID", nullable = false)
    private Article article;

    @Column(name = "Tag", nullable = false, length = 100)
    private String tag;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}