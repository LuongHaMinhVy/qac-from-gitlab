package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Notifications")
public class Notification extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account recipient;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", length = 30)
    private NotificationType type;

    @Column(name = "IsRead")
    private Boolean isRead = false;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}