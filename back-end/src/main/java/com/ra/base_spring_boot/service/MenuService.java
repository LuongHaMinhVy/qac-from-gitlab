package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.req.MenuRequest;
import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.MenuResponse;
import com.ra.base_spring_boot.model.constants.MenuLocation;

import java.util.List;

public interface MenuService {
    ApiResponse<MenuResponse> createMenu(MenuRequest request);

    ApiResponse<MenuResponse> updateMenu(Long id, MenuRequest request);

    ApiResponse<String> deleteMenu(Long id);

    ApiResponse<MenuResponse> getMenuById(Long id);

    ApiResponse<List<MenuResponse>> getAllMenus();

    ApiResponse<List<MenuResponse>> getMenusByLocation(MenuLocation location);
}
