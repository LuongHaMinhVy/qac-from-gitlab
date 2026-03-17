package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseDTO {
    private Integer roleId;
    private RoleName roleCode;
    private String roleName;
    private String description;
    private Boolean isSystem;
    private LocalDateTime createdAt;
}
