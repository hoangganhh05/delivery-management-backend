package com.viettel.deliverymanagement.service;

import com.viettel.deliverymanagement.entity.NotificationEntity;

import java.util.List;

public interface NotificationService {

    List<NotificationEntity> getMyNotifications(String username);

    long getUnreadCount(String username);

    void markAsRead(Long notificationId, String username);

    void markAllAsRead(String username);

    NotificationEntity createNotification(Long userId, String title, String message, String type, Long refId);
}
