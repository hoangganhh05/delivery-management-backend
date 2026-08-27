package com.viettel.deliverymanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyVoucherRequest {

    @Schema(description = "Mã voucher cần áp dụng", example = "VIETTEL50K")
    @NotBlank(message = "Mã voucher không được để trống")
    private String voucherCode;

    @Schema(description = "Tổng giá trị đơn hàng trước khi giảm", example = "250000")
    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị đơn hàng phải lớn hơn 0")
    private BigDecimal orderAmount;

    @Schema(description = "Phí vận chuyển dùng làm cơ sở tính giảm", example = "30000")
    @DecimalMin(value = "0.0", inclusive = false, message = "Phí vận chuyển phải lớn hơn 0")
    private BigDecimal shippingFee;
}
