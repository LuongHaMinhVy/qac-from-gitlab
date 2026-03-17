package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuth2LinkRequest {
    @NotBlank(message = "Provider không được để trống")
    private String provider;

    @NotBlank(message = "Access token không được để trống")
    private String accessToken;
}