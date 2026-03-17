package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.ReferenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleReferenceRequest {
    @NotNull(message = "Article ID is required")
    private Long articleId;

    private String citation;

    private String author;

    @Pattern(regexp = "\\d{4}", message = "Publication year must be 4 digits")
    private String publicationYear;

    private String title;

    @NotNull(message = "Reference type is required")
    private ReferenceType type;

    private String source;
    private String pages;
    private String citationStyle;

    private String url;
}
