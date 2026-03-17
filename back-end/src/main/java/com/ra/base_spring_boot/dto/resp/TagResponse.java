package com.ra.base_spring_boot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TagResponse {

    private Long id;
    private String tag;
    private Long articleId;
    private String articleTitle;
    private LocalDateTime createdAt;
}
