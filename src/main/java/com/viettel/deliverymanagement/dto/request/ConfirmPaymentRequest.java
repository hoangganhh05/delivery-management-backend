package com.viettel.deliverymanagement.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmPaymentRequest {
    @NotBlank(message = "Cần nhập mã giao dịch đã đối soát")
    @Size(max = 100) private String reference;
}
