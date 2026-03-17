package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.VideoSource;
import com.ra.base_spring_boot.model.constants.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String videoUrl;
    private MediaResponseDTO thumbnail;
    private String embedCode;
    private Integer duration;
    private String categoryName;
    private Long categoryId;
    private String authorName;
    private Long authorId;
    private VideoStatus status;
    private VideoSource source;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
