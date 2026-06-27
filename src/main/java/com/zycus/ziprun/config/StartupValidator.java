package com.zycus.ziprun.config;

import com.zycus.ziprun.routing.RoutingStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupValidator {

    private final Map<String, RoutingStrategy> strategyRegistry;

    @Value("${routing.strategy:AI}")
    private String configuredStrategy;

    @Value("${llm.api-key:}")
    private String llmApiKey;

    @PostConstruct
    public void validate() {
        log.info("=== ZipRun Startup Validation ===");

        // Validate routing strategy exists
        String strategyKey = configuredStrategy.toUpperCase();
        if (!strategyRegistry.containsKey(strategyKey)) {
            throw new IllegalStateException(
                    "Configured routing strategy '" + configuredStrategy + "' not found. " +
                            "Available strategies: " + strategyRegistry.keySet()
            );
        }
        log.info("Active routing strategy: {}", strategyKey);

        // Warn if LLM key looks like placeholder
        if (llmApiKey.isBlank() || llmApiKey.equals("your-gemini-api-key-here")) {
            log.warn("GEMINI_API_KEY not configured — AI strategy will fall back to rule-based on every call.");
        } else {
            log.info("LLM API key present — AI routing strategy enabled.");
        }

        log.info("=== Validation complete ===");
    }
}