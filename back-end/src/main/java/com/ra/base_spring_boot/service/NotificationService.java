package com.ra.base_spring_boot.service;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.NotificationResponse;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.constants.NotificationType;

import java.util.List;

public interface NotificationService {
    void createNotification(Account recipient, String title, String message, NotificationType type);

    void notifyArticleStatusChange(Account recipient, String articleTitle, String status);

    ApiResponse<List<NotificationResponse>> getNotifications(int page, int size, String sort, String direction);

    ApiResponse<String> markAsRead(Long id);
}
