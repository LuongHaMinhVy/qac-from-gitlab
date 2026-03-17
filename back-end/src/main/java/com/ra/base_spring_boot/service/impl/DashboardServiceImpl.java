package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.DashboardChartResponse;
import com.ra.base_spring_boot.dto.resp.DashboardSummaryResponse;
import com.ra.base_spring_boot.model.constants.ArticleStatus;
import com.ra.base_spring_boot.repository.ArticleRepository;
import com.ra.base_spring_boot.repository.CommentRepository;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final IAccountRepository accountRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Override
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        Long totalUsers = accountRepository.count();
        Long newUsersToday = accountRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        Long totalArticles = articleRepository.count();
        Long publishedArticles = articleRepository.countByStatus(ArticleStatus.published);
        Long pendingArticles = articleRepository.countByStatus(ArticleStatus.pending_review);

        Long totalViews = articleRepository.sumViewCount();
        if (totalViews == null)
            totalViews = 0L;

        Long totalComments = commentRepository.count();

        LocalDateTime startOfThisMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime startOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);
        LocalDateTime endOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        long articlesThisMonth = articleRepository.countByCreatedAtBetween(startOfThisMonth, now);
        long articlesLastMonth = articleRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        Double articleGrowth = calculateGrowth(articlesThisMonth, articlesLastMonth);

        long usersThisMonth = accountRepository.countByCreatedAtBetween(startOfThisMonth, now);
        long usersLastMonth = accountRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        Double userGrowth = calculateGrowth(usersThisMonth, usersLastMonth);

        DashboardSummaryResponse response = DashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .newUsersToday(newUsersToday)
                .totalArticles(totalArticles)
                .publishedArticles(publishedArticles)
                .pendingArticles(pendingArticles)
                .totalViews(totalViews)
                .totalComments(totalComments)
                .articlesGrowth(articleGrowth)
                .usersGrowth(userGrowth)
                .build();

        return ApiResponse.success(response, "Lấy báo cáo tổng quan thành công");
    }

    @Override
    public ApiResponse<DashboardChartResponse> getChartData() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);

        List<Object[]> articleData = articleRepository.countArticlesByMonth(sixMonthsAgo);

        List<DashboardChartResponse.ChartDataPoint> articleChart = new ArrayList<>();
        Map<Integer, Long> articleMap = new HashMap<>();

        for (Object[] row : articleData) {
            if (row != null && row.length >= 2) {
                Integer month = row[0] instanceof Number ? ((Number) row[0]).intValue() : null;
                Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                if (month != null) {
                    articleMap.put(month, count);
                }
            }
        }

        for (int i = 0; i < 6; i++) {
            LocalDateTime date = LocalDateTime.now().minusMonths(5 - i);
            int month = date.getMonthValue();
            String label = "T" + month;

            articleChart.add(DashboardChartResponse.ChartDataPoint.builder()
                    .date(label)
                    .count(articleMap.getOrDefault(month, 0L))
                    .build());
        }

        return ApiResponse.success(DashboardChartResponse.builder()
                .articleGrowth(articleChart)
                .userGrowth(new ArrayList<>())
                .build(), "Lấy dữ liệu biểu đồ thành công");
    }

    private Double calculateGrowth(long current, long previous) {
        if (previous == 0)
            return current > 0 ? 100.0 : 0.0;
        return ((double) (current - previous) / previous) * 100;
    }
}
