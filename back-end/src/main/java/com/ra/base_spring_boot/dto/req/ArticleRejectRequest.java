package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleRejectRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}
