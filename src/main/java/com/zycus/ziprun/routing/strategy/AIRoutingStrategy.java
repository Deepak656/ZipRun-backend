package com.zycus.ziprun.routing.strategy;

import com.zycus.ziprun.ai.*;
import com.zycus.ziprun.common.enums.RoutingStrategyType;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.routing.RoutingStrategy;
import com.zycus.ziprun.routing.dto.RoutingContext;
import com.zycus.ziprun.routing.dto.RoutingRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI-powered routing strategy backed by Gemini 1.5 Flash.
 * Falls back to the rule-based strategy on any failure — timeout,
 * malformed response, hallucinated agent ID, or API quota exhaustion.
 * The fallback is always a real suggestion, never a silent drop.
 */
@Slf4j
@Component("AI")
@RequiredArgsConstructor
public class AIRoutingStrategy implements RoutingStrategy {

    private final LLMGateway llmGateway;
    private final PromptBuilder promptBuilder;
    private final AIResponseParser responseParser;
    private final RuleBasedRoutingStrategy ruleBasedFallback;

    @Override
    public RoutingRecommendation recommend(RoutingContext context) {
        PromptType promptType = context.isRecoveryMode() ? PromptType.RECOVERY : PromptType.INITIAL;

        try {
            String prompt   = promptBuilder.buildPrompt(context, promptType);
            String rawText  = llmGateway.callLLM(prompt);
            Optional<AIResponse> parsed = responseParser.parse(rawText);

            if (parsed.isEmpty()) {
                log.warn("AI response could not be parsed for order {}. Falling back to rule-based.", context.getOrder().getId());
                return fallback(context, "LLM response could not be parsed as valid JSON.");
            }

            AIResponse aiResponse = parsed.get();

            // Validate the agent ID actually exists in our roster
            Map<String, Agent> agentIndex = context.getAvailableAgents().stream()
                    .collect(Collectors.toMap(Agent::getId, Function.identity()));

            Agent recommended = agentIndex.get(aiResponse.getAgentId());
            if (recommended == null) {
                log.warn("AI recommended agent '{}' which does not exist in available roster. Falling back.", aiResponse.getAgentId());
                return fallback(context, "AI recommended an agent ID that does not exist: " + aiResponse.getAgentId());
            }

            if (!recommended.isAvailable() || !recommended.hasCapacity()) {
                log.warn("AI recommended agent {} who is unavailable or at capacity. Falling back.", recommended.getId());
                return fallback(context, "AI recommended agent " + recommended.getId() + " who is unavailable or at capacity.");
            }

            log.info("AI strategy selected agent {} (confidence: {}) for order {}",
                    recommended.getId(), aiResponse.getConfidence(), context.getOrder().getId());

            return RoutingRecommendation.builder()
                    .recommendedAgent(recommended)
                    .confidence(aiResponse.getConfidence())
                    .reasoning(aiResponse.getReasoning())
                    .strategy(RoutingStrategyType.AI)
                    .fallbackUsed(false)
                    .build();

        } catch (Exception e) {
            log.error("AI routing strategy failed for order {}: {}. Falling back to rule-based.",
                    context.getOrder().getId(), e.getMessage(), e);
            return fallback(context, "AI call failed: " + e.getMessage());
        }
    }

    private RoutingRecommendation fallback(RoutingContext context, String failureReason) {
        RoutingRecommendation ruleResult = ruleBasedFallback.recommend(context);
        return RoutingRecommendation.builder()
                .recommendedAgent(ruleResult.getRecommendedAgent())
                .confidence(ruleResult.getConfidence())
                .reasoning(ruleResult.getReasoning())
                .strategy(RoutingStrategyType.AI)
                .fallbackUsed(true)
                .failureReason(failureReason)
                .build();
    }

    @Override
    public String getStrategyKey() {
        return "AI";
    }
}