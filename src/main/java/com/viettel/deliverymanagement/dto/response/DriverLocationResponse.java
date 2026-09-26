package com.viettel.deliverymanagement.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/** Latest GPS point, returned only after the order-access check succeeds. */
@Getter
@Builder
public class DriverLocationResponse {
    private Long orderId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracyMeters;
    private Instant reportedAt;
    private Instant receivedAt;
}
