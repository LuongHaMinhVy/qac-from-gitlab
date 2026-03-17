package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.constants.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "RoleCode", nullable = false, unique = true, length = 50)
    private RoleName roleCode;

    @Column(name = "RoleName", nullable = false, length = 100)
    private String roleName;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "IsSystem")
    private Boolean isSystem = false;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<AccountRole> accountRoles;

    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<RolePermission> rolePermissions;
}