package com.zycus.ziprun.ai;

import com.zycus.ziprun.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIResponseParser {

    private final JsonUtils jsonUtils;

    /**
     * Parses the raw LLM text into an AIResponse.
     * Returns empty if the response cannot be parsed — caller handles fallback.
     */
    public Optional<AIResponse> parse(String llmResponse) {
        if (llmResponse == null || llmResponse.isBlank()) {
            log.warn("LLM returned empty response");
            return Optional.empty();
        }

        Optional<AIResponse> result = jsonUtils.parseJson(llmResponse, AIResponse.class);

        if (result.isEmpty()) {
            log.warn("Could not parse LLM response as AIResponse JSON. Raw response: {}", llmResponse);
        }

        return result;
    }
}