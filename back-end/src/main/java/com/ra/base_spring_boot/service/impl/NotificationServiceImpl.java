package com.ra.base_spring_boot.service.impl;

import com.ra.base_spring_boot.dto.resp.ApiResponse;
import com.ra.base_spring_boot.dto.resp.NotificationResponse;
import com.ra.base_spring_boot.exception.CustomException;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.Notification;
import com.ra.base_spring_boot.model.constants.NotificationType;
import com.ra.base_spring_boot.repository.NotificationRepository;
import com.ra.base_spring_boot.service.NotificationService;
import com.ra.base_spring_boot.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

        private final NotificationRepository notificationRepository;
        private final SimpMessagingTemplate messagingTemplate;

        @Override
        public void createNotification(Account recipient, String title, String message, NotificationType type) {
                Notification notification = Notification.builder()
                                .recipient(recipient)
                                .title(title)
                                .message(message)
                                .type(type)
                                .isRead(false)
                                .createdAt(LocalDateTime.now())
                                .build();
                Notification saved = notificationRepository.save(notification);

                try {
                        NotificationResponse response = NotificationResponse.builder()
                                        .id(saved.getId())
                                        .title(saved.getTitle())
                                        .message(saved.getMessage())
                                        .type(saved.getType())
                                        .isRead(saved.getIsRead())
                                        .createdAt(saved.getCreatedAt())
                                        .build();

                        messagingTemplate.convertAndSendToUser(
                                        recipient.getUsername(),
                                        "/queue/notifications",
                                        response);
                } catch (Exception e) {
                }
        }

        @Override
        public void notifyArticleStatusChange(Account recipient, String articleTitle, String status) {
                String title = "Cập nhật trạng thái bài viết";
                String message = String.format("Bài viết '%s' của bạn đã chuyển sang trạng thái: %s", articleTitle,
                                status);
                createNotification(recipient, title, message, NotificationType.system);
        }

        @Override
        public ApiResponse<List<NotificationResponse>> getNotifications(
                        int page, int size, String sort, String direction) {

                Integer accountId = SecurityUtils.getCurrentAccountId();
                if (accountId == null) {
                        throw new CustomException(
                                        "User not authenticated", HttpStatus.UNAUTHORIZED);
                }

                Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                Pageable pageable = PageRequest.of(
                                page, size, Sort.by(sortDirection, sort));

                Page<Notification> notificationPage = notificationRepository
                                .findAllByRecipientAccountId(accountId, pageable);

                List<NotificationResponse> responses = notificationPage.getContent()
                                .stream()
                                .map(n -> NotificationResponse.builder()
                                                .id(n.getId())
                                                .title(n.getTitle())
                                                .message(n.getMessage())
                                                .type(n.getType())
                                                .isRead(n.getIsRead())
                                                .createdAt(n.getCreatedAt())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());

                return ApiResponse.success(responses, "Lấy danh sách thông báo thành công");
        }

        @Override
        public ApiResponse<String> markAsRead(Long id) {
                Integer accountId = SecurityUtils.getCurrentAccountId();
                if (accountId == null) {
                        throw new CustomException(
                                        "User not authenticated", HttpStatus.UNAUTHORIZED);
                }

                Notification notification = notificationRepository.findById(id)
                                .orElseThrow(() -> new CustomException(
                                                "Không tìm thấy thông báo", HttpStatus.NOT_FOUND));

                if (!notification.getRecipient().getAccountId().equals(accountId)) {
                        throw new CustomException(
                                        "Quyền truy cập bị từ chối", HttpStatus.FORBIDDEN);
                }

                notification.setIsRead(true);
                notificationRepository.save(notification);

                return ApiResponse.success(null, "Đã đánh dấu đã đọc");
        }
}
