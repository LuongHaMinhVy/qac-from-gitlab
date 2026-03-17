package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.SyncResponseDTO;

public interface SyncService {
    ApiResponse<SyncResponseDTO> getSyncData();
}
