package com.ra.base_spring_boot.model;

import com.ra.base_spring_boot.model.base.BaseObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "Analytics")
public class Analytics extends BaseObject {

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "TotalAccounts")
    private Integer totalAccounts = 0;

    @Column(name = "NewAccounts")
    private Integer newAccounts = 0;

    @Column(name = "TotalArticles")
    private Integer totalArticles = 0;

    @Column(name = "PublishedArticles")
    private Integer publishedArticles = 0;

    @Column(name = "TotalComments")
    private Integer totalComments = 0;

    @Column(name = "TotalViews")
    private Integer totalViews = 0;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}