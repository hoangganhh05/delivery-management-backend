package com.viettel.deliverymanagement.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter @Builder
public class ShipmentOfferResponse {
    private Long offerId;
    private Long orderId;
    private String trackingNumber;
    private String receiverName;
    private String receiverAddress;
    private LocalDateTime expiresAt;
}
