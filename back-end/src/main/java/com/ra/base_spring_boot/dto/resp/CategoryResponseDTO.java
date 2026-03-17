package com.ra.base_spring_boot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponseDTO {
    private Long id;

    private String name;

    private String description;

    private MediaResponseDTO coverImage;

    private Integer displayOrder;
    private Boolean status;
    private java.time.LocalDateTime createdAt;
}