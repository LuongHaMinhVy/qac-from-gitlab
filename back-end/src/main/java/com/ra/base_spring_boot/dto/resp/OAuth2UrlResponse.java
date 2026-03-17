package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UrlResponse {
    private SocialProvider provider;
    private String loginUrl;
    private String authorizationUrl;
}