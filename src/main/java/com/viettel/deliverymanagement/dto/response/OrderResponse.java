package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponse {
    private Long id;
    private String trackingNumber;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private BigDecimal shippingFee;
    private BigDecimal discountFee;
    private BigDecimal totalFee;
    private BigDecimal totalPrice;
    private BigDecimal codAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
