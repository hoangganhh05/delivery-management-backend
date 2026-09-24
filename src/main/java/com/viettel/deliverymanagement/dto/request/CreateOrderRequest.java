package com.viettel.deliverymanagement.dto.request;

import com.viettel.deliverymanagement.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(message = "Tên người gửi không được để trống")
    private String senderName;

    @NotBlank(message = "Số điện thoại người gửi không được để trống")
    private String senderPhone;

    @NotBlank(message = "Địa chỉ người gửi không được để trống")
    private String senderAddress;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ người nhận không được để trống")
    private String receiverAddress;

    @NotNull(message = "Cân nặng không được để trống")
    @Positive(message = "Cân nặng phải lớn hơn 0")
    private Integer weightGram;

    @NotNull(message = "Phí vận chuyển không được để trống")
    @Positive(message = "Phí vận chuyển phải lớn hơn 0")
    private BigDecimal shippingFee;

    @Pattern(regexp = "STANDARD|EXPRESS", message = "Gói giao hàng không hợp lệ")
    private String serviceType = "STANDARD";

    private BigDecimal codAmount;

    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private String voucherCode;

    @Valid
    private List<OrderItemRequest> items;
}
