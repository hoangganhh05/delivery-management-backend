package com.viettel.deliverymanagement.dto.request;
import com.viettel.deliverymanagement.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSearchRequest {
    private String keyword; // Tìm theo trackingNumber, senderPhone, receiverPhone
    private OrderStatus status; // Lọc theo trạng thái đơn hàng
    private Integer page = 0; // Trang hiện tại (mặc định trang 0)
    private Integer size = 10; // Số bản ghi / trang (mặc định 10)
}