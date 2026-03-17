package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private MediaResponseDTO featuredImage;
    private ArticleStatus status;
    private Boolean isHighlight;
    private Boolean isFeatured;
    private Boolean allowComments;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String authorName;
    private String categoryName;
    private Long categoryId;

    private String hashtag;

    private List<ArticleResponse> relatedArticles;
}
