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
@Table(name = "MediaLibrary")
public class Media extends BaseObject {

    @Column(name = "FileName", nullable = false, length = 255)
    private String fileName;

    @Column(name = "OriginalName", nullable = false, length = 255)
    private String originalName;

    @Column(name = "FileURL", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "MimeType", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "FileSize")
    private Long fileSize;

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account uploader;

    @Column(name = "AltText", length = 200)
    private String altText;

    @Column(name = "Caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "PublicId", length = 255)
    private String publicId;

    @Builder.Default
    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    @Builder.Default
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}