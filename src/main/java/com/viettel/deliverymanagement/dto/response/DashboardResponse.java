package com.viettel.deliverymanagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse implements Serializable {

    @Schema(description = "Tổng số lượng đơn hàng", example = "1500")
    private Long totalOrders;

    @Schema(description = "Số lượng đơn hàng giao thành công", example = "1350")
    private Long deliveredOrders;

    @Schema(description = "Số lượng đơn hàng bị hủy", example = "50")
    private Long cancelledOrders;

    @Schema(description = "Tổng doanh thu từ các đơn hàng giao thành công", example = "45000000.00")
    private BigDecimal totalRevenue;
}
