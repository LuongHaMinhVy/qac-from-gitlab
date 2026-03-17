package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import com.ra.base_spring_boot.model.constants.ExportStatus;
import com.ra.base_spring_boot.model.constants.ExportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Exports")
public class Export extends BaseObject {

    @ManyToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", length = 20)
    private ExportType type;

    @Column(name = "FileName", nullable = false, length = 255)
    private String fileName;

    @Column(name = "FilePath", nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private ExportStatus status = ExportStatus.pending;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "CompletedAt")
    private LocalDateTime completedAt;
}
