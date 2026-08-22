package com.viettel.deliverymanagement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse implements Serializable {

    @Schema(description = "Đường dẫn URL cổng thanh toán VNPay", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...")
    private String paymentUrl;

    @Schema(description = "Thông báo kết quả", example = "Tạo URL thanh toán thành công")
    private String message;

    @Schema(description = "Trạng thái giao dịch", example = "SUCCESS")
    private String status;
}
