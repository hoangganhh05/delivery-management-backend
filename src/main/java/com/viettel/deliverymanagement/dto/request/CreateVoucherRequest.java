package com.viettel.deliverymanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVoucherRequest {

    @Schema(description = "Mã voucher", example = "VIETTEL50K")
    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @Schema(description = "Phần trăm giảm giá", example = "10")
    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @Min(value = 1, message = "Phần trăm giảm giá phải lớn hơn 0")
    @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100")
    private Integer discountPercent;

    @Schema(description = "Số tiền giảm giá tối đa", example = "50000")
    @DecimalMin(value = "0.0", message = "Số tiền giảm giá tối đa không được âm")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "Giá trị đơn hàng tối thiểu", example = "100000")
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderAmount;

    @Schema(description = "Ngày bắt đầu hiệu lực", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Ngày kết thúc hiệu lực", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Số lượt sử dụng tối đa", example = "100")
    @Min(value = 1, message = "Số lượt sử dụng phải lớn hơn 0")
    private Integer usageLimit;
}
