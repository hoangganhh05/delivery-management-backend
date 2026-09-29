package com.viettel.deliverymanagement.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        // 1. Quét biến môi trường hệ thống từ mọi định dạng (Railway, Docker, Heroku, v.v.)
        if (!isConfigured()) {
            String[] envVars = {"GEMINI_API_KEY", "gemini_api_key", "GEMINI_KEY", "gemini_key", "GOOGLE_API_KEY"};
            for (String var : envVars) {
                String val = System.getenv(var);
                if (val != null && !val.trim().isEmpty()) {
                    this.apiKey = val.trim();
                    log.info("Đã tìm thấy Gemini API Key từ biến môi trường hệ thống: {}", var);
                    break;
                }
            }
        }
        if (!isConfigured()) {
            String sysProp = System.getProperty("GEMINI_API_KEY");
            if (sysProp != null && !sysProp.trim().isEmpty()) {
                this.apiKey = sysProp.trim();
            }
        }
        // 2. Nạp từ file .env cục bộ nếu chạy dưới local
        if (!isConfigured()) {
            try {
                Path[] paths = new Path[]{
                        Paths.get(".env"),
                        Paths.get("../.env"),
                        Paths.get("delivery-management-backend/.env")
                };
                for (Path p : paths) {
                    if (Files.exists(p)) {
                        for (String line : Files.readAllLines(p)) {
                            line = line.trim();
                            if ((line.startsWith("GEMINI_API_KEY=") || line.startsWith("GEMINI_KEY=")) && !isConfigured()) {
                                int eqIdx = line.indexOf('=');
                                this.apiKey = line.substring(eqIdx + 1).trim().replace("\"", "").replace("'", "");
                            }
                            if (line.startsWith("GEMINI_MODEL=")) {
                                int eqIdx = line.indexOf('=');
                                this.model = line.substring(eqIdx + 1).trim().replace("\"", "").replace("'", "");
                            }
                        }
                        if (isConfigured()) {
                            log.info("Đã nạp Gemini API Key từ file cục bộ: {}", p.toAbsolutePath());
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        if (isConfigured()) {
            log.info("Google Gemini AI client đã sẵn sàng với model: {}", this.model);
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
