package com.ra.base_spring_boot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionCheckResponse {
    private Boolean hasPermission;
    private String permissionCode;
    private String message;
    private String[] userRoles;
    private String[] userPermissions;
}

