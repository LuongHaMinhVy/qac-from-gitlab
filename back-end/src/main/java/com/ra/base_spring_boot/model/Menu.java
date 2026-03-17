package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.MenuLocation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Menus")
public class Menu extends BaseObject {

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Location", length = 20)
    private MenuLocation location;

    @Column(name = "Items", nullable = false, columnDefinition = "TEXT")
    private String items;

    @Column(name = "IsActive")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "CreatedBy", nullable = false)
    private Account createdBy;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
}