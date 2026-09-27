package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    @NotBlank(message = "Tên mặt hàng không được để trống")
    @Size(max = 150, message = "Tên mặt hàng không được dài quá 150 ký tự")
    private String itemName;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    @DecimalMax(value = "1000", message = "Số lượng không được vượt quá 1000")
    private Integer quantity;

    @NotNull(message = "Cân nặng không được để trống")
    @Positive(message = "Cân nặng phải lớn hơn 0")
    @DecimalMax(value = "100000", message = "Cân nặng mặt hàng không được vượt quá 100kg")
    private Integer weightGram;

    @NotNull(message = "Giá trị khai báo không được để trống")
    @Positive(message = "Giá trị khai báo phải lớn hơn 0")
    @DecimalMax(value = "1000000000", message = "Giá trị khai báo không hợp lệ")
    private BigDecimal declaredValue;
}
