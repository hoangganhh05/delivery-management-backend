package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** A GPS point reported by the assigned shipper's device. */
@Getter
@Setter
public class UpdateShipmentLocationRequest {

    @NotNull
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    @PositiveOrZero
    private BigDecimal accuracy;

    /** Device timestamp for display/audit; the server still records its own receipt time. */
    @PastOrPresent
    private Instant timestamp;
}
