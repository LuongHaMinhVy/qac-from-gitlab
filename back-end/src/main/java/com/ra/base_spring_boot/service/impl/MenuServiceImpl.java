package com.ra.base_spring_boot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra.base_spring_boot.dto.req.MenuRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MenuResponse;
import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Menu;
import com.ra.base_spring_boot.model.constants.MenuLocation;
import com.ra.base_spring_boot.repository.MenuRepository;
import com.ra.base_spring_boot.service.MenuService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponse<MenuResponse> createMenu(MenuRequest request) {
        validateMenuJson(request.getItems());
        Account currentAccount = SecurityUtils.getCurrentAccount();

        boolean shouldBeActive = request.getIsActive() != null ? request.getIsActive() : true;
        if (shouldBeActive) {
            deactivateOtherMenus(request.getLocation());
        }

        Menu menu = Menu.builder()
                .name(request.getName())
                .location(request.getLocation())
                .items(request.getItems())
                .isActive(shouldBeActive)
                .createdBy(currentAccount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Menu savedMenu = menuRepository.save(menu);
        return ApiResponse.success(mapToResponse(savedMenu), "Tạo menu thành công");
    }

    @Override
    @Transactional
    public ApiResponse<MenuResponse> updateMenu(Long id, MenuRequest request) {
        validateMenuJson(request.getItems());
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy menu với id: " + id));

        boolean wasActive = menu.getIsActive();
        boolean willBeActive = request.getIsActive() != null ? request.getIsActive() : wasActive;

        if (willBeActive && (!wasActive || menu.getLocation() != request.getLocation())) {
            deactivateOtherMenus(request.getLocation());
        }

        menu.setName(request.getName());
        menu.setLocation(request.getLocation());
        menu.setItems(request.getItems());
        menu.setIsActive(willBeActive);
        menu.setUpdatedAt(LocalDateTime.now());

        Menu updatedMenu = menuRepository.save(menu);
        return ApiResponse.success(mapToResponse(updatedMenu), "Cập nhật menu thành công");
    }

    private void validateMenuJson(String json) {
        if (json == null || json.isBlank()) {
            throw new HttpBadRequest("Nội dung menu không được để trống");
        }
        try {
            objectMapper.readTree(json);
            if (!json.trim().startsWith("[")) {
                throw new HttpBadRequest("Định dạng menu phải là một mảng JSON (Array)");
            }
        } catch (Exception e) {
            throw new HttpBadRequest("Chuỗi JSON menu không hợp lệ: " + e.getMessage());
        }
    }

    private void deactivateOtherMenus(MenuLocation location) {
        List<Menu> activeMenus = menuRepository.findAllByLocationAndIsActiveTrue(location);
        for (Menu m : activeMenus) {
            m.setIsActive(false);
            m.setUpdatedAt(LocalDateTime.now());
            menuRepository.save(m);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new HttpNotFound("Không tìm thấy menu với id: " + id);
        }
        menuRepository.deleteById(id);
        return ApiResponse.success(null, "Xóa menu thành công");
    }

    @Override
    public ApiResponse<MenuResponse> getMenuById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Không tìm thấy menu với id: " + id));
        return ApiResponse.success(mapToResponse(menu), "Lấy thông tin menu thành công");
    }

    @Override
    public ApiResponse<List<MenuResponse>> getAllMenus() {
        List<MenuResponse> menus = menuRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        if (menus.isEmpty()) {
            throw new HttpNotFound("Hiện tại chưa có menu nào trong hệ thống");
        }

        return ApiResponse.success(menus, "Lấy danh sách menu thành công");
    }

    @Override
    public ApiResponse<List<MenuResponse>> getMenusByLocation(MenuLocation location) {
        List<MenuResponse> menus = menuRepository.findAllByLocationAndIsActiveTrue(location).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        if (menus.isEmpty()) {
            throw new HttpNotFound("Chưa có menu nào khả dụng tại vị trí: " + location);
        }

        return ApiResponse.success(menus, "Lấy danh sách menu theo vị trí thành công");
    }

    private MenuResponse mapToResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .location(menu.getLocation())
                .items(menu.getItems())
                .isActive(menu.getIsActive())
                .createdBy(menu.getCreatedBy() != null ? menu.getCreatedBy().getUsername() : null)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}
