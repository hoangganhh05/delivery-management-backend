package com.viettel.deliverymanagement.dto.request;
import com.viettel.deliverymanagement.constant.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Getter
@Setter
public class OrderSearchRequest {
    private String keyword; // Tìm theo trackingNumber, senderPhone, receiverPhone
    private OrderStatus status; // Lọc theo trạng thái đơn hàng
    @Min(value = 0, message = "Trang phải lớn hơn hoặc bằng 0")
    private Integer page = 0; // Trang hiện tại (mặc định trang 0)

    @Min(value = 1, message = "Kích thước trang phải lớn hơn 0")
    @Max(value = 100, message = "Kích thước trang không được vượt quá 100")
    private Integer size = 10; // Số bản ghi / trang (mặc định 10)
}
