package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingResponse implements Serializable {

    @Schema(description = "Mã vận đơn tra cứu", example = "VT1A2B3C4D")
    private String trackingNumber;

    @Schema(description = "Tên người gửi", example = "Nguyễn Văn A")
    private String senderName;

    @Schema(description = "Tên người nhận", example = "Trần Thị B")
    private String receiverName;

    @Schema(description = "Trạng thái hiện tại của đơn hàng", example = "IN_TRANSIT")
    private OrderStatus currentStatus;

    @Schema(description = "Cước phí vận chuyển", example = "35000.00")
    private BigDecimal shippingFee;

    @Schema(description = "Tiền thu hộ COD", example = "200000.00")
    private BigDecimal codAmount;

    @Schema(description = "Tổng chi phí đơn hàng", example = "35000.00")
    private BigDecimal totalFee;

    @Schema(description = "Danh sách lịch sử hành trình vận đơn")
    private List<ShipmentHistoryDto> history;
}
