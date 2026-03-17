package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "ActivityLogs")
public class ActivityLog extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "AccountID")
    private Account account;

    @Column(name = "Action", nullable = false, length = 100)
    private String action;

    @Column(name = "Details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}