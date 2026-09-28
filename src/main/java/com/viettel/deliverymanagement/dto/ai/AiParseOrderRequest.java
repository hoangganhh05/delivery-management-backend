package com.viettel.deliverymanagement.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiParseOrderRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String text;
}
