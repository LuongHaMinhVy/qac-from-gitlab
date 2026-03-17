package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.InteractionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInteractionRequest {
    private Long articleId;
    private Long videoId;

    @NotNull(message = "Loại tương tác không được để trống")
    private InteractionType interactionType;
}
