package com.viettel.deliverymanagement.dto.request;

import com.viettel.deliverymanagement.constant.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShipmentStatusRequest {

    @Schema(description = "Trạng thái mới của đơn hàng", example = "PICKED_UP")
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;

    @Schema(description = "Ghi chú quá trình giao hàng", example = "Đã nhận hàng từ người gửi")
    private String note;

    @Schema(description = "Đường dẫn ảnh bằng chứng giao hàng (nếu có)", example = "https://cdn.delivery.viettel.vn/proof/order_123.jpg")
    private String proofImageUrl;
}
