package com.viettel.deliverymanagement.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    CREATED("Đơn hàng mới tạo"),
    ASSIGNED("Đã phân công shipper"),
    PICKED_UP("Shipper đã lấy hàng"),
    IN_TRANSIT("Đang vận chuyển"),
    DELIVERED("Giao hàng thành công"),
    CANCELLED("Đơn hàng đã hủy");

    private final String description;

}
