package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.SeverityLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadWordRequest {
    @NotBlank(message = "Từ khóa không được để trống")
    private String word;

    private String replacement;

    private SeverityLevel severity;

    private Boolean isActive;
}
