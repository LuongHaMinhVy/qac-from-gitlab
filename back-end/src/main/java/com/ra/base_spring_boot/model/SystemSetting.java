package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.SettingCategory;
import com.ra.base_spring_boot.model.constants.SettingType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "SystemSettings")
public class SystemSetting extends BaseObject {

    @Column(name = "SettingKey", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(name = "Value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Category", nullable = false, length = 20)
    private SettingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 20)
    private SettingType type;

    @Column(name = "IsPublic", nullable = false)
    private Boolean isPublic = true;

    @ManyToOne
    @JoinColumn(name = "UpdatedBy")
    private Account updatedBy;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}