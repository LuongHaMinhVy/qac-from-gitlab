package com.ra.base_spring_boot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private Long totalUsers;
    private Long newUsersToday;

    private Long totalArticles;
    private Long publishedArticles;
    private Long pendingArticles;

    private Long totalViews;
    private Long totalComments;

    private Double articlesGrowth;
    private Double usersGrowth;
}
