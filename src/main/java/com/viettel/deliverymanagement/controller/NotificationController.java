package com.viettel.deliverymanagement.controller;

import com.viettel.deliverymanagement.dto.response.ResponseData;
import com.viettel.deliverymanagement.entity.NotificationEntity;
import com.viettel.deliverymanagement.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs thông báo hệ thống và đơn hàng")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của người dùng hiện tại")
    public ResponseData<List<NotificationEntity>> getMyNotifications(Authentication authentication) {
        String username = authentication.getName();
        List<NotificationEntity> list = notificationService.getMyNotifications(username);
        return ResponseData.success("Lấy danh sách thông báo thành công", list);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Lấy số lượng thông báo chưa đọc")
    public ResponseData<Long> getUnreadCount(Authentication authentication) {
        String username = authentication.getName();
        long count = notificationService.getUnreadCount(username);
        return ResponseData.success("Lấy số lượng thông báo chưa đọc thành công", count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc")
    public ResponseData<String> markAsRead(@PathVariable Long id, Authentication authentication) {
        notificationService.markAsRead(id, authentication.getName());
        return ResponseData.success("Đánh dấu đã đọc thành công", "SUCCESS");
    }

    @PutMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseData<String> markAllAsRead(Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAllAsRead(username);
        return ResponseData.success("Đánh dấu tất cả đã đọc thành công", "SUCCESS");
    }
}
