package com.viettel.deliverymanagement.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.viettel.deliverymanagement.config.GeminiConfig;
import com.viettel.deliverymanagement.dto.ai.AiChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiClientService {

    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Gọi Google Gemini API với cơ chế tự động chuyển đổi mô hình (Model Failover)
     */
    public Optional<String> generateContent(String systemInstruction, String userPrompt, List<AiChatMessage> history, boolean jsonOutput) {
        if (!geminiConfig.isConfigured()) {
            log.debug("Gemini API Key chưa được cấu hình. Sử dụng chế độ Heuristic Fallback.");
            return Optional.empty();
        }

        List<String> candidateModels = List.of(
                geminiConfig.getModel(),
                "gemini-2.5-flash",
                "gemini-2.0-flash",
                "gemini-1.5-flash-latest"
        ).stream().distinct().toList();

        for (String modelName : candidateModels) {
            Optional<String> result = callGeminiApi(modelName, systemInstruction, userPrompt, history, jsonOutput);
            if (result.isPresent()) {
                return result;
            }
        }

        log.warn("Tất cả các mô hình Gemini đều không thể phản hồi yêu cầu.");
        return Optional.empty();
    }

    private Optional<String> callGeminiApi(String modelName, String systemInstruction, String userPrompt, List<AiChatMessage> history, boolean jsonOutput) {
        try {
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    geminiConfig.getBaseUrl().replaceAll("/+$", ""),
                    modelName,
                    geminiConfig.getApiKey());

            ObjectNode root = objectMapper.createObjectNode();

            // System Instruction
            if (systemInstruction != null && !systemInstruction.isBlank()) {
                ObjectNode sysInstructionNode = root.putObject("systemInstruction");
                ArrayNode sysParts = sysInstructionNode.putArray("parts");
                sysParts.addObject().put("text", systemInstruction);
            }

            // Contents (History + Current Prompt)
            ArrayNode contentsArray = root.putArray("contents");

            if (history != null && !history.isEmpty()) {
                for (AiChatMessage msg : history) {
                    if (msg.getContent() == null || msg.getContent().isBlank()) continue;
                    ObjectNode contentNode = contentsArray.addObject();
                    String role = "model".equalsIgnoreCase(msg.getRole()) || "assistant".equalsIgnoreCase(msg.getRole()) ? "model" : "user";
                    contentNode.put("role", role);
                    contentNode.putArray("parts").addObject().put("text", msg.getContent());
                }
            }

            // Current prompt
            ObjectNode currentContent = contentsArray.addObject();
            currentContent.put("role", "user");
            currentContent.putArray("parts").addObject().put("text", userPrompt);

            // Generation Config
            ObjectNode genConfig = root.putObject("generationConfig");
            genConfig.put("temperature", jsonOutput ? 0.1 : 0.6);
            genConfig.put("maxOutputTokens", 1200);
            if (jsonOutput) {
                genConfig.put("responseMimeType", "application/json");
            }

            String requestBody = objectMapper.writeValueAsString(root);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode resJson = objectMapper.readTree(response.body());
                JsonNode candidates = resJson.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode partsNode = candidates.get(0).path("content").path("parts");
                    if (partsNode.isArray() && !partsNode.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonNode part : partsNode) {
                            if (part.has("text")) {
                                sb.append(part.path("text").asText());
                            }
                        }
                        if (sb.length() > 0) {
                            return Optional.of(sb.toString().trim());
                        }
                    }
                }
            } else {
                log.warn("Gemini model {} trả về status {}: {}", modelName, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết nối tới Gemini model {}: {}", modelName, e.getMessage());
        }

        return Optional.empty();
    }
}
