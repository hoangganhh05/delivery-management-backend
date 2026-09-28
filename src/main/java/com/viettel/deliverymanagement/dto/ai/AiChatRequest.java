package com.viettel.deliverymanagement.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatRequest {

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String message;

    private List<AiChatMessage> history;

    private String trackingNumber;
}
