package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Categories")
@Builder
public class Category extends BaseObject {

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "CoverImageID")
    private Media coverImage;

    @Builder.Default
    @Column(name = "DisplayOrder")
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "Status")
    private Boolean status = true;

    @ManyToOne
    @JoinColumn(name = "CreatedBy", nullable = false)
    private Account createdBy;

    @Builder.Default
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();
}