package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "SocialLogins")
public class SocialLogin extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "Provider", nullable = false, length = 20)
    private SocialProvider provider;

    @Column(name = "ProviderID", nullable = false, length = 100)
    private String providerId;

    @Column(name = "ConnectedAt", updatable = false)
    private LocalDateTime connectedAt = LocalDateTime.now();

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Name", length = 100)
    private String name;

    @Column(name = "Avatar", length = 500)
    private String avatar;

    @Column(name = "AccessToken", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "RefreshToken", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "TokenExpiresAt")
    private LocalDateTime tokenExpiresAt;
}