package com.ra.base_spring_boot.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardChartResponse {
    private List<ChartDataPoint> userGrowth;
    private List<ChartDataPoint> articleGrowth;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChartDataPoint {
        private String date;
        private Long count;
    }
}
