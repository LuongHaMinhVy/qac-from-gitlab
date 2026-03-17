package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.CommentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Comments")
public class Comment extends BaseObject {

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account author;

    @ManyToOne
    @JoinColumn(name = "ArticleID")
    private Article article;

    @ManyToOne
    @JoinColumn(name = "VideoID")
    private Video video;

    @ManyToOne
    @JoinColumn(name = "ParentID")
    private Comment parent;

    @Column(name = "Depth")
    private Integer depth = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private CommentStatus status = CommentStatus.pending;

    @Column(name = "LikeCount")
    private Integer likeCount = 0;

    @Column(name = "IsEdited")
    private Boolean isEdited = false;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
