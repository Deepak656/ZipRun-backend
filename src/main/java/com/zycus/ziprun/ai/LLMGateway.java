package com.zycus.ziprun.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LLMGateway {

    @Value("${llm.provider:gemini}")
    private String provider;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:gemini-1.5-flash}")
    private String model;

    @Value("${llm.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    private final RestClient http = RestClient.create();

    /**
     * Sends the prompt to the configured LLM provider and returns the raw text response.
     * Throws RuntimeException on HTTP error, timeout, or unparseable structure.
     * Callers are responsible for JSON parsing, validation, and fallback logic.
     */
    public String callLLM(String prompt) {
        log.debug("Calling LLM provider: {} with model: {}", provider, model);
        return switch (provider.toLowerCase()) {
            case "gemini" -> callGemini(prompt);
            case "groq"   -> callOpenAICompatible(prompt, baseUrl + "/openai/v1/chat/completions");
            case "ollama" -> callOpenAICompatible(prompt, baseUrl + "/v1/chat/completions");
            default       -> throw new IllegalStateException("Unknown LLM provider: " + provider);
        };
    }

    private String callGemini(String prompt) {
        String url = baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 512
                )
        );

        try {
            Map<?, ?> response = http.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> content  = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts      = (List<?>) content.get("parts");
            String text        = (String) ((Map<?, ?>) parts.get(0)).get("text");

            log.debug("Gemini raw response: {}", text);
            return text;

        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    private String callOpenAICompatible(String prompt, String url) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3,
                "max_tokens", 512
        );

        try {
            Map<?, ?> response = http.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            throw new RuntimeException("LLM API call failed: " + e.getMessage(), e);
        }
    }
}