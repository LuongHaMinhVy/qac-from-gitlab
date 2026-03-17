package com.ra.base_spring_boot.dto.req;

import com.ra.base_spring_boot.model.constants.RoleName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MemberSearchRequest {

        @Size(max = 255, message = "Search must not exceed 255 characters")
        private String email;

        @Pattern(regexp = "^$|^(active|inactive|verified|unverified)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Status must be one of: active, inactive, verified, unverified")
        private String status;

        @Pattern(regexp = "^$|^(ROLE_ADMIN|ROLE_USER|ROLE_AUTHOR|ROLE_EDITOR)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Role must be one of: ROLE_ADMIN, ROLE_USER, ROLE_AUTHOR, ROLE_EDITOR")
        private String role;

        private Boolean isActive;

        @Min(value = 0, message = "Page must be >= 0")
        private Integer page = 0;

        @Min(value = 1, message = "Size must be > 0")
        @Max(value = 100, message = "Size must not exceed 100")
        private Integer size = 10;

        @Pattern(regexp = "^$|^(fullName|email|username|createdAt|updatedAt)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Sort must be one of: fullName, email, username, createdAt, updatedAt")
        private String sort = "createdAt";

        @Pattern(regexp = "^$|^(asc|desc)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Direction must be 'asc' or 'desc'")
        private String direction = "desc";
}
