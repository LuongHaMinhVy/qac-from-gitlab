package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "APILogs")
public class ApiLog extends BaseObject {

    @Column(name = "Endpoint", nullable = false, length = 200)
    private String endpoint;

    @Column(name = "Method", nullable = false, length = 10)
    private String method;

    @ManyToOne
    @JoinColumn(name = "AccountID")
    private Account account;

    @Column(name = "IPAddress", length = 45)
    private String ipAddress;

    @Column(name = "UserAgent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "StatusCode")
    private Integer statusCode;

    @Column(name = "ResponseTime")
    private Integer responseTime;

    @Column(name = "RequestHeaders", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "RequestBody", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "ResponseBody", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}