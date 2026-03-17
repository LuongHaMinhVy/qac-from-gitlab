package com.ra.base_spring_boot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.req.BulkSystemSettingRequest;
import com.ra.base_spring_boot.dto.req.SystemSettingRequest;
import com.ra.base_spring_boot.dto.req.SystemSettingUpdateRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.Pagination;
import com.ra.base_spring_boot.dto.resp.SystemSettingResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.SystemSetting;
import com.ra.base_spring_boot.model.constants.SettingCategory;
import com.ra.base_spring_boot.model.constants.SettingType;
import com.ra.base_spring_boot.repository.SystemSettingRepository;
import com.ra.base_spring_boot.service.SystemSettingService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository settingRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> PROTECTED_SETTINGS = Arrays.asList(
            "site_name", "site_logo", "contact_email");

    private static final String EMAIL_REGEX = "^[A-Za-z][A-Za-z0-9]{4,}@gmail\\.com$"; // From ValidEmailImpl
    private static final String PHONE_REGEX = "^(0[3|5|7|8|9][0-9]{8}|(\\+84)[3|5|7|8|9][0-9]{8})$"; // From
                                                                                                     // ValidPhoneImpl
    private static final String URL_REGEX = "^(https?://)?[\\w.-]+\\.[a-z]{2,}(/.*)?$";

    @Override
    @Transactional
    public ApiResponse<SystemSettingResponse> createSetting(SystemSettingRequest request) {
        if (settingRepository.existsBySettingKey(request.getSettingKey())) {
            throw new HttpConflict("Key '" + request.getSettingKey() + "' đã tồn tại trong hệ thống");
        }

        validateValueByType(request.getValue(), request.getType());

        Account currentAccount = SecurityUtils.getCurrentAccount();

        SystemSetting setting = SystemSetting.builder()
                .settingKey(request.getSettingKey())
                .value(request.getValue())
                .description(request.getDescription())
                .category(request.getCategory())
                .type(request.getType())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .updatedBy(currentAccount)
                .build();

        SystemSetting savedSetting = settingRepository.save(setting);
        log.info("Created new setting: key={}, category={}, by={}",
                savedSetting.getSettingKey(),
                savedSetting.getCategory(),
                currentAccount != null ? currentAccount.getUsername() : "system");

        return ApiResponse.success(mapToResponse(savedSetting), "Tạo cài đặt thành công");
    }

    @Override
    @Transactional
    public ApiResponse<SystemSettingResponse> updateSetting(Long id, SystemSettingUpdateRequest request) {
        SystemSetting setting = settingRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy cài đặt với ID: " + id));

        return updateSettingInternal(setting, request);
    }

    @Override
    @Transactional
    public ApiResponse<SystemSettingResponse> updateSettingByKey(String key, SystemSettingUpdateRequest request) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy cài đặt với key: " + key));

        return updateSettingInternal(setting, request);
    }

    private ApiResponse<SystemSettingResponse> updateSettingInternal(SystemSetting setting,
            SystemSettingUpdateRequest request) {
        validateValueByType(request.getValue(), setting.getType());

        Account currentAccount = SecurityUtils.getCurrentAccount();

        setting.setValue(request.getValue());
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            setting.setIsPublic(request.getIsPublic());
        }
        setting.setUpdatedBy(currentAccount);

        SystemSetting updatedSetting = settingRepository.save(setting);
        log.info("Updated setting: key={}, by={}",
                updatedSetting.getSettingKey(),
                currentAccount != null ? currentAccount.getUsername() : "system");

        return ApiResponse.success(mapToResponse(updatedSetting), "Cập nhật cài đặt thành công");
    }

    @Override
    @Transactional
    public ApiResponse<List<SystemSettingResponse>> bulkUpdateSettings(BulkSystemSettingRequest request) {
        List<String> keys = request.getSettings().stream()
                .map(BulkSystemSettingRequest.SettingKeyValue::getSettingKey)
                .collect(Collectors.toList());

        Map<String, SystemSetting> existingSettings = settingRepository.findBySettingKeyIn(keys).stream()
                .collect(Collectors.toMap(SystemSetting::getSettingKey, s -> s));

        List<SystemSettingResponse> updatedResponses = new ArrayList<>();
        List<String> notFoundKeys = new ArrayList<>();
        Account currentAccount = SecurityUtils.getCurrentAccount();

        for (BulkSystemSettingRequest.SettingKeyValue kv : request.getSettings()) {
            SystemSetting setting = existingSettings.get(kv.getSettingKey());
            if (setting == null) {
                notFoundKeys.add(kv.getSettingKey());
                continue;
            }

            validateValueByType(kv.getValue(), setting.getType());

            setting.setValue(kv.getValue());
            if (kv.getDescription() != null) {
                setting.setDescription(kv.getDescription());
            }
            if (kv.getIsPublic() != null) {
                setting.setIsPublic(kv.getIsPublic());
            }
            setting.setUpdatedBy(currentAccount);

            SystemSetting updatedSetting = settingRepository.save(setting);
            updatedResponses.add(mapToResponse(updatedSetting));
        }

        if (!notFoundKeys.isEmpty()) {
            log.warn("Bulk update: keys not found: {}", notFoundKeys);
        }

        String message = notFoundKeys.isEmpty()
                ? "Cập nhật hàng loạt thành công"
                : "Cập nhật thành công. Các key không tìm thấy: " + String.join(", ", notFoundKeys);

        log.info("Bulk updated {} settings by {}",
                updatedResponses.size(),
                currentAccount != null ? currentAccount.getUsername() : "system");

        return ApiResponse.success(updatedResponses, message);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteSetting(Long id) {
        SystemSetting setting = settingRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy cài đặt với ID: " + id));

        if (PROTECTED_SETTINGS.contains(setting.getSettingKey())) {
            throw new HttpBadRequest("Không thể xóa cài đặt quan trọng: " + setting.getSettingKey());
        }

        settingRepository.delete(setting);
        log.info("Deleted setting: key={}", setting.getSettingKey());

        return ApiResponse.success(null, "Xóa cài đặt thành công");
    }

    @Override
    public ApiResponse<SystemSettingResponse> getSettingById(Long id) {
        SystemSetting setting = settingRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy cài đặt với ID: " + id));

        return ApiResponse.success(mapToResponse(setting), "Lấy thông tin cài đặt thành công");
    }

    @Override
    public ApiResponse<SystemSettingResponse> getSettingByKey(String key) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy cài đặt với key: " + key));

        return ApiResponse.success(mapToResponse(setting), "Lấy thông tin cài đặt thành công");
    }

    @Override
    public ApiResponse<List<SystemSettingResponse>> getAllSettings(String keyword, SettingCategory category,
            Pageable pageable) {
        Page<SystemSetting> settingsPage = settingRepository.searchSettings(keyword, category, pageable);

        List<SystemSettingResponse> responses = settingsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Pagination pagination = Pagination.builder()
                .currentPage(settingsPage.getNumber() + 1)
                .pageSize(settingsPage.getSize())
                .totalElements(settingsPage.getTotalElements())
                .totalPages(settingsPage.getTotalPages())
                .build();

        return ApiResponse.success(responses, "Lấy danh sách cài đặt thành công", pagination);
    }

    @Override
    public ApiResponse<List<SystemSettingResponse>> getSettingsByCategory(SettingCategory category) {
        List<SystemSettingResponse> responses = settingRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        if (responses.isEmpty()) {
            throw new HttpNotFound("Không có cài đặt nào trong danh mục: " + category);
        }

        return ApiResponse.success(responses, "Lấy danh sách cài đặt theo danh mục thành công");
    }

    @Override
    public ApiResponse<List<SystemSettingResponse>> getPublicSettings() {
        List<SystemSettingResponse> responses = settingRepository.findByIsPublicTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses, "Lấy danh sách cài đặt public thành công");
    }

    @Override
    public ApiResponse<List<SystemSettingResponse>> getPublicSettingsByCategory(SettingCategory category) {
        List<SystemSettingResponse> responses = settingRepository.findByCategoryAndIsPublicTrue(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses, "Lấy danh sách cài đặt public theo danh mục thành công");
    }

    private void validateValueByType(String value, SettingType type) {
        if (value == null || value.isBlank()) {
            throw new HttpBadRequest("Value không được để trống");
        }

        switch (type) {
            case EMAIL:
                if (!value.matches(EMAIL_REGEX)) {
                    throw new HttpBadRequest("Value phải là định dạng email hợp lệ (Gmail)");
                }
                break;
            case URL:
            case IMAGE:
                if (!value.startsWith("/") && !value.matches(URL_REGEX)) {
                    throw new HttpBadRequest("Value phải là URL hoặc đường dẫn hợp lệ");
                }
                break;
            case PHONE:
                if (!value.matches(PHONE_REGEX)) {
                    throw new HttpBadRequest("Value phải là số điện thoại Việt Nam hợp lệ");
                }
                break;
            case NUMBER:
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new HttpBadRequest("Value phải là số hợp lệ");
                }
                break;
            case BOOLEAN:
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new HttpBadRequest("Value phải là 'true' hoặc 'false'");
                }
                break;
            case JSON:
                try {
                    objectMapper.readTree(value);
                } catch (Exception e) {
                    throw new HttpBadRequest("Value phải là chuỗi JSON hợp lệ: " + e.getMessage());
                }
                break;
            case TEXT:
            case TEXTAREA:
            default:
                break;
        }
    }

    private SystemSettingResponse mapToResponse(SystemSetting setting) {
        return SystemSettingResponse.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .value(setting.getValue())
                .description(setting.getDescription())
                .category(setting.getCategory())
                .type(setting.getType())
                .isPublic(setting.getIsPublic())
                .updatedBy(setting.getUpdatedBy() != null ? setting.getUpdatedBy().getUsername() : null)
                .createdAt(setting.getCreatedAt())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
