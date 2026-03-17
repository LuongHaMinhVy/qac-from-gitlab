package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.resp.ApiLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiLogService {
    ApiResponse<List<ApiLogResponseDTO>> getLogs(
            Integer accountId,
            String method,
            Integer statusCode,
            String endpoint,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
}
