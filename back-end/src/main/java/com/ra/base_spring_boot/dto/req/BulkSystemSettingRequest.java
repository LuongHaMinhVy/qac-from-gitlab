package com.ra.base_spring_boot.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkSystemSettingRequest {

    @NotEmpty(message = "Danh sách settings không được để trống")
    @Valid
    private List<SettingKeyValue> settings;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SettingKeyValue {
        @NotBlank(message = "Key không được để trống")
        private String settingKey;

        @NotBlank(message = "Value không được để trống")
        private String value;

        private String description;

        private Boolean isPublic;
    }
}
