package com.viettel.deliverymanagement.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionCode {
    VIEW_ORDERS("Đơn hàng", "Xem đơn hàng"),
    CREATE_ORDER("Đơn hàng", "Tạo đơn hàng"),
    EDIT_ORDER("Đơn hàng", "Chỉnh sửa đơn hàng"),
    CANCEL_ORDER("Đơn hàng", "Hủy đơn hàng"),
    ASSIGN_SHIPPER("Đơn hàng", "Phân công shipper"),
    VIEW_SHIPPERS("Shipper", "Xem danh sách shipper"),
    MANAGE_SHIPPERS("Shipper", "Quản lý shipper"),
    UPDATE_DELIVERY("Shipper", "Cập nhật trạng thái giao hàng"),
    VIEW_USERS("Người dùng", "Xem người dùng"),
    MANAGE_USERS("Người dùng", "Quản lý người dùng"),
    MANAGE_ROLES("Người dùng", "Quản lý vai trò"),
    VIEW_PAYMENTS("Tài chính", "Xem thanh toán"),
    MANAGE_PAYMENTS("Tài chính", "Quản lý thanh toán"),
    MANAGE_VOUCHERS("Tài chính", "Quản lý voucher"),
    APPLY_VOUCHER("Tài chính", "Sử dụng voucher"),
    VIEW_REPORTS("Báo cáo & Quản lý", "Xem báo cáo"),
    EXPORT_DATA("Báo cáo & Quản lý", "Xuất dữ liệu"),
    SYSTEM_SETTINGS("Báo cáo & Quản lý", "Cài đặt ứng dụng"),
    VIEW_NOTIFICATIONS("Báo cáo & Quản lý", "Xem thông báo");

    private final String group;
    private final String label;
}
