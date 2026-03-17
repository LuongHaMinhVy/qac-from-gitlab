package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.ApprovalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "RoleRequests")
public class RoleRequest extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "RequestedRoleID", nullable = false)
    private Role requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private ApprovalStatus status = ApprovalStatus.pending;

    @Column(name = "Reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "ReviewedBy")
    private Account reviewedBy;

    @Column(name = "ReviewComments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "ReviewedAt")
    private LocalDateTime reviewedAt;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
