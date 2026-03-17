package com.ra.base_spring_boot.service;

import java.util.List;

import com.ra.base_spring_boot.dto.resp.ActivityLogResponseDTO;
import com.ra.base_spring_boot.dto.resp.ApiResponse;

public interface ActivitiLogService {
    ApiResponse<List<ActivityLogResponseDTO>> getLogs(int page, int size, String sortBy, String sortDir,
            Integer accountId, String action, String keyword);
}
