package com.ra.base_spring_boot.dto.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentRequest {
    @NotEmpty(message = "At least one role must be provided")
    private Set<String> roleCodes;
}
