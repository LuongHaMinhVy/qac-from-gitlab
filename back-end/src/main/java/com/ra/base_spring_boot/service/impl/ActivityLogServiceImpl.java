package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.model.ActivityLog;
import com.ra.base_spring_boot.repository.ActivityLogRepo;
import com.ra.base_spring_boot.service.ActivitiLogService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivitiLogService {

        private final ActivityLogRepo activityLogRepo;

        @Override
        public ApiResponse<List<ActivityLogResponseDTO>> getLogs(int page,
                        int size, String sortBy, String sortDir,
                        Integer accountId, String action, String keyword) {

                Sort sort = "asc".equalsIgnoreCase(sortDir)
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);

                Page<ActivityLog> result = activityLogRepo.search(
                                accountId,
                                action,
                                (keyword == null || keyword.isBlank()) ? null : keyword.trim(),
                                pageable);

                List<ActivityLogResponseDTO> responseList = result.map(al -> ActivityLogResponseDTO.builder()
                                .id(al.getId())
                                .accountId(al.getAccount() != null ? al.getAccount().getAccountId() : null)
                                .action(al.getAction())
                                .details(al.getDetails())
                                .createdAt(al.getCreatedAt())
                                .build()).getContent();

                Pagination pagination = Pagination
                                .builder()
                                .currentPage(result.getNumber())
                                .pageSize(result.getSize())
                                .totalElements(result.getTotalElements())
                                .totalPages(result.getTotalPages())
                                .build();

                return ApiResponse.success(responseList,
                                "Get activity logs successfully", pagination);
        }
}
