package com.ra.base_spring_boot.dto.resp;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogResponseDTO {
    private Long id;
    private String endpoint;
    private String method;
    private Integer accountId;
    private String username;
    private String ipAddress;
    private Integer statusCode;
    private Integer responseTime;
    private LocalDateTime createdAt;
    private String requestHeaders;
    private String requestBody;
    private String responseBody;
}
