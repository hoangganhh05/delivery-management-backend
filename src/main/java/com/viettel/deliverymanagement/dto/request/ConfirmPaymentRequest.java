package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmPaymentRequest {
    @Size(max = 100) private String reference;
}
