package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.resp.*;
import com.ra.base_spring_boot.repository.NotificationRepository;
import com.ra.base_spring_boot.service.*;
import com.ra.base_spring_boot.service.member.MemberService;
import com.ra.base_spring_boot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final MemberService memberService;
    private final NotificationRepository notificationRepository;
    private final MenuService menuService;
    private final CategoryService categoryService;
    private final SystemSettingService settingService;

    @Override
    public ApiResponse<SyncResponseDTO> getSyncData() {
        Integer accountId = SecurityUtils.getCurrentAccountId();

        MemberResponse user = null;
        Long unreadCount = 0L;

        if (accountId != null) {
            try {
                user = memberService.getCurrentMember().getData();
                unreadCount = notificationRepository.countByRecipientAccountIdAndIsReadFalse(accountId);
            } catch (Exception e) {
            }
        }

        List<MenuResponse> menus = menuService.getAllMenus().getData();
        List<CategoryResponseDTO> categories = categoryService.getAllCategories("", 0, 1000, "createdAt", "desc")
                .getData();
        List<SystemSettingResponse> settings = settingService.getPublicSettings().getData();

        SyncResponseDTO syncData = SyncResponseDTO.builder()
                .user(user)
                .unreadNotificationCount(unreadCount)
                .menus(menus)
                .categories(categories)
                .settings(settings)
                .build();

        return ApiResponse.success(syncData, "Sync data retrieved successfully");
    }
}
