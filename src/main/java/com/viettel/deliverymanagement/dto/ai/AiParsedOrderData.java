package com.viettel.deliverymanagement.dto.ai;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiParsedOrderData {

    private String senderName;
    private String senderPhone;
    private String senderAddress;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    // Phân rã địa chỉ hành chính (nếu nhận diện được)
    private String province;
    private String district;
    private String ward;
    private String streetAddress;

    private String itemName;
    private Integer weightGram;
    private BigDecimal declaredValue;
    private BigDecimal codAmount;
    private String note;

    private String parserSource; // "GEMINI_AI" hoặc "SMART_HEURISTIC"
    private String message;
}
