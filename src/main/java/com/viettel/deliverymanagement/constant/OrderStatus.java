package com.viettel.deliverymanagement.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CREATED("Đơn hàng mới tạo"),
    PENDING("Chờ xử lý"),
    PAID("Đã thanh toán"),
    ASSIGNED("Đã phân công shipper"),
    PICKED_UP("Shipper đã lấy hàng"),
    IN_TRANSIT("Đang vận chuyển"),
    SHIPPING("Đang giao hàng"),
    DELIVERED("Giao hàng thành công"),
    DONE("Hoàn thành"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đơn hàng đã hủy"),
    FAILED("Giao hàng thất bại");

    private final String description;

    /** Legacy data can contain any of these three values for a delivered order. */
    public static Set<OrderStatus> deliveryCompletedStatuses() {
        return Set.of(DELIVERED, DONE, COMPLETED);
    }

    public boolean isDeliveryCompleted() {
        return deliveryCompletedStatuses().contains(this);
    }

    public boolean isTerminal() {
        return isDeliveryCompleted() || this == CANCELLED || this == FAILED;
    }
}
