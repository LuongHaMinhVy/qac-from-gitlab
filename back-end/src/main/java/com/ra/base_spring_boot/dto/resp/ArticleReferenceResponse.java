package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleReferenceResponse {
    private Long id;
    private Long articleId;
    private String citation;
    private ReferenceType type;
    private String url;
    private LocalDateTime createdAt;
}
