package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.PaymentMethod;
import com.viettel.deliverymanagement.constant.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentRecordResponse {
    private Long orderId;
    private String trackingNumber;
    private String customerName;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private String reference;
    private LocalDateTime createdAt;
}
