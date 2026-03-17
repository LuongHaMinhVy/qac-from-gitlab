package com.ra.base_spring_boot.dto.resp;

import com.ra.base_spring_boot.model.constants.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RoleRequestResponse {

    private Long id;
    private Integer accountId;
    private String accountEmail;
    private String accountUsername;
    private String requestedRoleCode;
    private String requestedRoleName;
    private ApprovalStatus status;
    private String reason;
    private Integer reviewedById;
    private String reviewComments;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
