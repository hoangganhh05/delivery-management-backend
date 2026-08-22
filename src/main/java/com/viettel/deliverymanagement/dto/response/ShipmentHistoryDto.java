package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentHistoryDto implements Serializable {

    @Schema(description = "Trạng thái tại mốc lịch sử", example = "PICKED_UP")
    private OrderStatus status;

    @Schema(description = "Mô tả / Ghi chú tại mốc lịch sử", example = "Shipper đã lấy hàng từ kho Ba Đình")
    private String note;

    @Schema(description = "Hình ảnh bằng chứng (nếu có)", example = "https://cdn.delivery.viettel.vn/proof/proof1.jpg")
    private String proofImageUrl;

    @Schema(description = "Thời gian ghi nhận", example = "2026-08-23T08:30:00")
    private LocalDateTime timestamp;
}
