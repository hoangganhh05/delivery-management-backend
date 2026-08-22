package com.viettel.deliverymanagement.dto.request;

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
public class AssignShipperRequest {

    @Schema(description = "ID của đơn hàng cần phân công", example = "1")
    @NotNull(message = "ID đơn hàng không được để trống")
    private Long orderId;

    @Schema(description = "ID của shipper được phân công", example = "10")
    @NotNull(message = "ID shipper không được để trống")
    private Long shipperId;

    @Schema(description = "Ghi chú phân công", example = "Giao hàng trong giờ hành chính")
    private String note;
}