package com.viettel.deliverymanagement.service.impl;

import com.viettel.deliverymanagement.entity.NotificationEntity;
import com.viettel.deliverymanagement.entity.UserEntity;
import com.viettel.deliverymanagement.exception.AppException;
import com.viettel.deliverymanagement.repository.NotificationRepository;
import com.viettel.deliverymanagement.repository.UserRepository;
import com.viettel.deliverymanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationEntity> getMyNotifications(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));

        return notificationRepository.findByUserIdOrderByIdDesc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));

        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException("NOTIFICATION_NOT_FOUND", "Không tìm thấy thông báo"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));
        if (!user.getId().equals(notification.getUserId())) {
            throw new AppException("NOTIFICATION_ACCESS_DENIED", "Bạn không có quyền thay đổi thông báo này");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));

        List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByIdDesc(user.getId());
        for (NotificationEntity n : notifications) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationEntity createNotification(Long userId, String title, String message, String type, Long refId) {
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(refId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }
}
