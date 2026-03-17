package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.CommentStatus;
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
public class CommentResponse {
    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private Long parentId;
    private Integer depth;
    private Integer likeCount;
    private Boolean isEdited;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> mentionedUsers;
}
