package com.viettel.deliverymanagement.dto.ai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatMessage {
    private String role; // "user" hoặc "assistant" / "model"
    private String content;
}
