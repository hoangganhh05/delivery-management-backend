package com.viettel.deliverymanagement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {
    private Long id;
    private String itemName;
    private Integer quantity;
    private Integer weightGram;
    private BigDecimal declaredValue;
}
