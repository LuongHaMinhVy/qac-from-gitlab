package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String excerpt;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long mediaId;

    private ArticleStatus status;

    private Boolean isHighlight;

    private Boolean isFeatured;

    private Boolean allowComments;
    
    private String hashtag;
}
