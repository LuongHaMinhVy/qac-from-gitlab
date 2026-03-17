package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.BulkSystemSettingRequest;
import com.ra.base_spring_boot.dto.req.SystemSettingRequest;
import com.ra.base_spring_boot.dto.req.SystemSettingUpdateRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.SystemSettingResponse;
import com.ra.base_spring_boot.model.constants.SettingCategory;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SystemSettingService {
    ApiResponse<SystemSettingResponse> createSetting(SystemSettingRequest request);

    ApiResponse<SystemSettingResponse> updateSetting(Long id, SystemSettingUpdateRequest request);

    ApiResponse<SystemSettingResponse> updateSettingByKey(String key, SystemSettingUpdateRequest request);

    ApiResponse<List<SystemSettingResponse>> bulkUpdateSettings(BulkSystemSettingRequest request);

    ApiResponse<String> deleteSetting(Long id);

    ApiResponse<SystemSettingResponse> getSettingById(Long id);

    ApiResponse<SystemSettingResponse> getSettingByKey(String key);

    ApiResponse<List<SystemSettingResponse>> getAllSettings(String keyword, SettingCategory category,
            Pageable pageable);

    ApiResponse<List<SystemSettingResponse>> getSettingsByCategory(SettingCategory category);

    ApiResponse<List<SystemSettingResponse>> getPublicSettings();

    ApiResponse<List<SystemSettingResponse>> getPublicSettingsByCategory(SettingCategory category);
}
