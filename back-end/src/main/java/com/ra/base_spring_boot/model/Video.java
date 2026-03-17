package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.VideoSource;
import com.ra.base_spring_boot.model.constants.VideoStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Videos")
public class Video extends BaseObject {

    @Column(name = "Title", nullable = false, length = 500)
    private String title;

    @Column(name = "Slug", unique = true, length = 500)
    private String slug;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "VideoURL", nullable = false, length = 500)
    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "ThumbnailID")
    private Media thumbnail;

    @Enumerated(EnumType.STRING)
    @Column(name = "Source", length = 20)
    private VideoSource source;

    @Column(name = "EmbedCode", columnDefinition = "TEXT")
    private String embedCode;

    @Column(name = "Duration")
    private Integer duration;

    @ManyToOne
    @JoinColumn(name = "CategoryID")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account author;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private VideoStatus status = VideoStatus.draft;

    @Builder.Default
    @Column(name = "ViewCount")
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "LikeCount")
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "CommentCount")
    private Integer commentCount = 0;

    @Column(name = "PublishedAt")
    private LocalDateTime publishedAt;

    @Builder.Default
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserInteraction> userInteractions;
}
