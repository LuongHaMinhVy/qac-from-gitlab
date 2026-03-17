package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.VideoSource;
import com.ra.base_spring_boot.model.constants.VideoStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề không được vượt quá 500 ký tự")
    private String title;

    private String description;

    @Size(max = 500, message = "URL Video không được vượt quá 500 ký tự")
    private String videoUrl;

    private Long mediaId;

    private Long categoryId;

    private VideoStatus status;

    private VideoSource source;
}
