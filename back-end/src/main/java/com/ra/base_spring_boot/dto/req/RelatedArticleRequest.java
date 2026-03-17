package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.RelationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelatedArticleRequest {
    @NotNull(message = "Related article ID is required")
    private Long relatedArticleId;

    @NotNull(message = "Relation type is required")
    private RelationType relationType;
}
