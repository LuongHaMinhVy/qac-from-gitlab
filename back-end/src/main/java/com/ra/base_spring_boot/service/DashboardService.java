package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.DashboardChartResponse;
import com.ra.base_spring_boot.dto.resp.DashboardSummaryResponse;

public interface DashboardService {
    ApiResponse<DashboardSummaryResponse> getSummary();

    ApiResponse<DashboardChartResponse> getChartData();
}
