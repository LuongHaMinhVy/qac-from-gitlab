package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ArticleStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponse {
    private Long id;
    private Long articleId;
    private String articleTitle;
    private Integer reviewerId;
    private String reviewerName;
    private ArticleStatus oldStatus;
    private ArticleStatus newStatus;
    private String reason;
    private LocalDateTime createdAt;
}
