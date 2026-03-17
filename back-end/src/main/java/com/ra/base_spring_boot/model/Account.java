package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer accountId;

    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @Column(name = "EmailVerified")
    private Boolean emailVerified = false;

    @Column(name = "LastLoginAt")
    private LocalDateTime lastLoginAt;

    @Column(name = "LoginAttempts")
    private Integer loginAttempts = 0;

    @Column(name = "LockUntil")
    private LocalDateTime lockUntil;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SocialLogin> socialLogins;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "AccountRoles", joinColumns = @JoinColumn(name = "AccountID"), inverseJoinColumns = @JoinColumn(name = "RoleID"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AccountRole> accountRoles;

    public boolean hasSocialAccount(SocialProvider provider) {
        if (socialLogins == null)
            return false;
        return socialLogins.stream()
                .anyMatch(sl -> sl.getProvider() == provider);
    }
}