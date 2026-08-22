package com.viettel.deliverymanagement.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum
Role {
    ADMIN("Quản trị viên"),
    SHIPPER("Nhân viên giao hàng"),
    CUSTOMER("Khách hàng");

    private final String description;
}
