package com.viettel.deliverymanagement.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class GeminiConfig {

    @Value("${gemini.api-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${gemini.model:${GEMINI_MODEL:gemini-2.5-flash}}")
    private String model = "gemini-2.5-flash";

    @Value("${gemini.base-url:${GEMINI_BASE_URL:https://generativelanguage.googleapis.com/v1beta}}")
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("your_gemini_api_key");
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        // 1. Thử lấy từ System Environment (Railway, Docker, Heroku, etc.)
        if (!isConfigured()) {
            String envKey = System.getenv("GEMINI_API_KEY");
            if (envKey != null && !envKey.trim().isEmpty()) {
                this.apiKey = envKey.trim();
            }
        }
        if (!isConfigured()) {
            String sysProp = System.getProperty("GEMINI_API_KEY");
            if (sysProp != null && !sysProp.trim().isEmpty()) {
                this.apiKey = sysProp.trim();
            }
        }
        // 2. Thử đọc từ file .env cục bộ nếu chạy dưới local
        if (!isConfigured()) {
            try {
                java.nio.file.Path[] paths = new java.nio.file.Path[]{
                        java.nio.file.Paths.get(".env"),
                        java.nio.file.Paths.get("../.env"),
                        java.nio.file.Paths.get("delivery-management-backend/.env")
                };
                for (java.nio.file.Path p : paths) {
                    if (java.nio.file.Files.exists(p)) {
                        for (String line : java.nio.file.Files.readAllLines(p)) {
                            line = line.trim();
                            if (line.startsWith("GEMINI_API_KEY=") && !isConfigured()) {
                                this.apiKey = line.substring("GEMINI_API_KEY=".length()).trim().replace("\"", "").replace("'", "");
                            }
                            if (line.startsWith("GEMINI_MODEL=")) {
                                this.model = line.substring("GEMINI_MODEL=".length()).trim().replace("\"", "").replace("'", "");
                            }
                        }
                        if (isConfigured()) break;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (isConfigured()) {
            log.info("Google Gemini AI đã được khởi tạo thành công với model: {}", this.model);
        } else {
            log.warn("Gemini API Key chưa được cấu hình. Hệ thống sẽ hoạt động ở chế độ Heuristic Fallback.");
        }
    }

    @org.springframework.context.annotation.Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }
}
