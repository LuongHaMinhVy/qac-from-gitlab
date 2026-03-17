package com.ra.base_spring_boot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PermissionID")
    private Integer permissionId;

    @Column(name = "PermissionCode", nullable = false, unique = true, length = 100)
    private String permissionCode;

    @Column(name = "PermissionName", nullable = false, length = 100)
    private String permissionName;

    @Column(name = "Module", nullable = false, length = 50)
    private String module;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "permission")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<RolePermission> rolePermissions;
}