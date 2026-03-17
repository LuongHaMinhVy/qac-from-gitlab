package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.SeverityLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "BadWords")
public class BadWord extends BaseObject {

    @Column(name = "Word", nullable = false, unique = true, length = 100)
    private String word;

    @Column(name = "Replacement", length = 100)
    private String replacement;

    @Enumerated(EnumType.STRING)
    @Column(name = "Severity", length = 10)
    private SeverityLevel severity = SeverityLevel.medium;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "CreatedBy")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "roles", "accountRoles", "socialLogins", "passwordHash" })
    private Account createdBy;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
