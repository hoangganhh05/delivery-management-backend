package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    private BigDecimal codAmount;

    @Valid
    private List<OrderItemRequest> items;
}
