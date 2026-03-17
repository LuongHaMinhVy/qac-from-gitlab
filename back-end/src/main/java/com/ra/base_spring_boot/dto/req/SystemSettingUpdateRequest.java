package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemSettingUpdateRequest {

    @NotBlank(message = "Value không được để trống")
    private String value;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    private Boolean isPublic;
}
