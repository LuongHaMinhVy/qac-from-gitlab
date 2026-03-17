package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.resp.ApiLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.model.ApiLog;
import com.ra.base_spring_boot.repository.ApiLogRepository;
import com.ra.base_spring_boot.service.ApiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiLogServiceImpl implements ApiLogService {

        private final ApiLogRepository apiLogRepository;

        @Override
        public ApiResponse<List<ApiLogResponseDTO>> getLogs(Integer accountId, String method, Integer statusCode,
                        String endpoint, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
                Page<ApiLog> logsPage = apiLogRepository.searchLogs(accountId, method, statusCode, endpoint, startDate,
                                endDate,
                                pageable);

                List<ApiLogResponseDTO> responses = logsPage.getContent().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                Pagination pagination = Pagination.builder()
                                .currentPage(logsPage.getNumber() + 1)
                                .pageSize(logsPage.getSize())
                                .totalElements(logsPage.getTotalElements())
                                .totalPages(logsPage.getTotalPages())
                                .build();

                return ApiResponse.success(responses, "Get API logs successfully", pagination);
        }

        private ApiLogResponseDTO mapToResponse(ApiLog log) {
                return ApiLogResponseDTO.builder()
                                .id(log.getId())
                                .endpoint(log.getEndpoint())
                                .method(log.getMethod())
                                .accountId(log.getAccount() != null ? log.getAccount().getAccountId() : null)
                                .username(log.getAccount() != null ? log.getAccount().getUsername() : null)
                                .ipAddress(log.getIpAddress())
                                .statusCode(log.getStatusCode())
                                .responseTime(log.getResponseTime())
                                .createdAt(log.getCreatedAt())
                                .requestHeaders(log.getRequestHeaders())
                                .requestBody(log.getRequestBody())
                                .responseBody(log.getResponseBody())
                                .build();
        }
}
