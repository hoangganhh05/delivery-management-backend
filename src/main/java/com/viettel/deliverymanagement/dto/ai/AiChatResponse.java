package com.viettel.deliverymanagement.dto.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatResponse {

    private String reply;
    private String suggestedAction; // "TRACK_ORDER", "CREATE_ORDER", "CHECK_VOUCHER", "NONE"
    private Object actionData;
    private List<String> quickQuestions;
    private String source; // "GEMINI_AI" hoặc "ASSISTANT_KNOWLEDGE_BASE"
}
