package com.viettel.deliverymanagement.dto.response;

import com.viettel.deliverymanagement.constant.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ManualPaymentInstructionResponse {
    private Long orderId;
    private PaymentMethod method;
    private String title;
    private String providerName;
    private String recipientLabel;
    private String recipientValue;
    private String recipientName;
    private BigDecimal amount;
    private String transferContent;
    private String note;
}
