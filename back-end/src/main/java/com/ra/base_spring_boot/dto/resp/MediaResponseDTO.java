package com.ra.base_spring_boot.dto.resp;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaResponseDTO {
    private Long id;
    private String fileName;
    private String originalName;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private String altText;
    private String caption;
    private AccountResponseDTO uploader;
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private Boolean deduplicated;
}
