package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.SettingCategory;
import com.ra.base_spring_boot.model.constants.SettingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemSettingResponse {
    private Long id;
    private String settingKey;
    private String value;
    private String description;
    private SettingCategory category;
    private SettingType type;
    private Boolean isPublic;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
