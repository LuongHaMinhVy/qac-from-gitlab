package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccountResponse {
    private SocialProvider provider;
    private String providerId;
    private String email;
    private String name;
    private String avatar;
    private LocalDateTime connectedAt;
}