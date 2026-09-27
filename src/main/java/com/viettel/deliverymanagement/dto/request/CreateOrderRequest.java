package com.viettel.deliverymanagement.dto.request;

import com.viettel.deliverymanagement.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(message = "Tên người gửi không được để trống")
    @Size(max = 100, message = "Tên người gửi không được dài quá 100 ký tự")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]{1,99}$", message = "Tên người gửi chứa ký tự không hợp lệ")
    private String senderName;

    @NotBlank(message = "Số điện thoại người gửi không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)\\d{8}$", message = "Số điện thoại người gửi không hợp lệ")
    private String senderPhone;

    @NotBlank(message = "Địa chỉ người gửi không được để trống")
    @Size(max = 255, message = "Địa chỉ người gửi không được dài quá 255 ký tự")
    private String senderAddress;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận không được dài quá 100 ký tự")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]{1,99}$", message = "Tên người nhận chứa ký tự không hợp lệ")
    private String receiverName;

    @NotBlank(message = "Số điện thoại người nhận không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)\\d{8}$", message = "Số điện thoại người nhận không hợp lệ")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ người nhận không được để trống")
    @Size(max = 255, message = "Địa chỉ người nhận không được dài quá 255 ký tự")
    private String receiverAddress;

    @NotNull(message = "Cân nặng không được để trống")
    @Positive(message = "Cân nặng phải lớn hơn 0")
    @DecimalMax(value = "100000", message = "Cân nặng không được vượt quá 100kg")
    private Integer weightGram;

    @NotNull(message = "Khoảng cách giao hàng không được để trống")
    @Positive(message = "Khoảng cách giao hàng phải lớn hơn 0")
    @DecimalMax(value = "5000", message = "Khoảng cách giao hàng không hợp lệ")
    private BigDecimal distanceKm;

    @NotNull(message = "Phí vận chuyển không được để trống")
    @Positive(message = "Phí vận chuyển phải lớn hơn 0")
    @DecimalMax(value = "100000000", message = "Phí vận chuyển không hợp lệ")
    private BigDecimal shippingFee;

    @Pattern(regexp = "STANDARD|EXPRESS", message = "Gói giao hàng không hợp lệ")
    private String serviceType = "STANDARD";

    @DecimalMax(value = "1000000000", message = "Tiền thu hộ không hợp lệ")
    @jakarta.validation.constraints.PositiveOrZero(message = "Tiền thu hộ không được âm")
    private BigDecimal codAmount;

    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private String voucherCode;

    @Valid
    private List<OrderItemRequest> items;
}
