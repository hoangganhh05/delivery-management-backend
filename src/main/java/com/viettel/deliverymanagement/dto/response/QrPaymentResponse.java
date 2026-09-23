package com.viettel.deliverymanagement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class QrPaymentResponse {
    private Long orderId;
    private String bankId;
    private String accountNumber;
    private String accountName;
    private BigDecimal amount;
    private String transferContent;
}
