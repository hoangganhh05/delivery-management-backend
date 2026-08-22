package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    @NotBlank(message = "Tên mặt hàng không được để trống")
    private String itemName;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Cân nặng không được để trống")
    @Positive(message = "Cân nặng phải lớn hơn 0")
    private Integer weightGram;

    @NotNull(message = "Giá trị khai báo không được để trống")
    @Positive(message = "Giá trị khai báo phải lớn hơn 0")
    private BigDecimal declaredValue;
}
