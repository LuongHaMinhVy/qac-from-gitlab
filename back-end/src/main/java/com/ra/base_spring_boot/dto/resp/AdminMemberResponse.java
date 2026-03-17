package com.ra.base_spring_boot.dto.resp;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminMemberResponse {
    private Long userId;
    private Integer accountId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private MediaResponseDTO avatar;
    private Boolean isActive;
    private Boolean emailVerified;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}