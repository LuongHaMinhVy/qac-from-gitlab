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
@Entity
@Table(name = "Articles")
@Builder
public class Article extends BaseObject {

    @Column(name = "Title", nullable = false, length = 500)
    private String title;

    @Column(name = "Slug", nullable = false, unique = true, length = 500)
    private String slug;

    @Column(name = "Excerpt", columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "Hashtag", length = 255)
    private String hashtag;

    @Lob
    @Column(name = "Content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account author;

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private Category category;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private ArticleStatus status = ArticleStatus.draft;

    @ManyToOne
    @JoinColumn(name = "FeaturedImageID")
    private Media featuredImage;

    @Builder.Default
    @Column(name = "IsHighlight")
    private Boolean isHighlight = false;

    @Builder.Default
    @Column(name = "IsFeatured")
    private Boolean isFeatured = false;

    @Builder.Default
    @Column(name = "AllowComments")
    private Boolean allowComments = true;

    @Builder.Default
    @Column(name = "ViewCount")
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "LikeCount")
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "CommentCount")
    private Integer commentCount = 0;

    @Column(name = "ReadingTime")
    private Integer readingTime;

    @Column(name = "PublishedAt")
    private LocalDateTime publishedAt;

    @Builder.Default
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
}