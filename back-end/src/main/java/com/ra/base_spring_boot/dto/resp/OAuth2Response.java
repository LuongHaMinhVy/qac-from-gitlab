package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2Response {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private String fullName;
    private MediaResponseDTO avatar;
    private List<String> roles;
    private SocialProvider provider;
    private boolean isNewUser;
    private String message;
    private LocalDateTime expiresAt;
}