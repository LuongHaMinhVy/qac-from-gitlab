package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.SettingCategory;
import com.ra.base_spring_boot.model.constants.SettingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemSettingRequest {

    @NotBlank(message = "Key không được để trống")
    @Size(min = 3, max = 100, message = "Key phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-z][a-z0-9_]*$", message = "Key phải bắt đầu bằng chữ cái thường, chỉ chứa chữ thường, số và dấu gạch dưới")
    private String settingKey;

    @NotBlank(message = "Value không được để trống")
    private String value;

    @NotNull(message = "Category không được để trống")
    private SettingCategory category;

    @NotNull(message = "Type không được để trống")
    private SettingType type;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    private Boolean isPublic = true;
}
