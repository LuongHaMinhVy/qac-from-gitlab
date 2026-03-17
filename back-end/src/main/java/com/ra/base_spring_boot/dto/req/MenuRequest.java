package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.MenuLocation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuRequest {
    @NotBlank(message = "Tên menu không được để trống")
    private String name;

    @NotNull(message = "Vị trí menu không được để trống")
    private MenuLocation location;

    @NotBlank(message = "Nội dung menu không được để trống")
    private String items;

    private Boolean isActive;
}
