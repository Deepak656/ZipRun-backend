package com.zycus.ziprun.routing.strategy;

import com.zycus.ziprun.common.enums.RoutingStrategyType;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.routing.RoutingStrategy;
import com.zycus.ziprun.routing.dto.RoutingContext;
import com.zycus.ziprun.routing.dto.RoutingRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Rule-based fallback strategy. Recommends the available agent
 * with the fewest active orders who has remaining capacity and
 * can handle the order's weight class.
 *
 * This strategy has no external dependencies — it always produces
 * a result as long as at least one eligible agent exists.
 */
@Slf4j
@Component("RULE")
public class RuleBasedRoutingStrategy implements RoutingStrategy {

    @Override
    public RoutingRecommendation recommend(RoutingContext context) {
        List<Agent> eligible = context.getAvailableAgents().stream()
                .filter(Agent::isAvailable)
                .filter(Agent::hasCapacity)
                .filter(a -> context.getOrder().getWeightClass() == null
                        || a.canHandle(context.getOrder().getWeightClass()))
                .sorted(Comparator.comparingInt(Agent::getActiveOrderCount))
                .toList();

        Optional<Agent> best = eligible.stream().findFirst();

        if (best.isEmpty()) {
            log.warn("Rule-based strategy found no eligible agent for order: {}", context.getOrder().getId());
            return RoutingRecommendation.builder()
                    .confidence(0.0)
                    .reasoning("No eligible agent found. All agents are either offline, at capacity, or unable to handle this weight class.")
                    .strategy(RoutingStrategyType.RULE)
                    .fallbackUsed(false)
                    .build();
        }

        Agent agent = best.get();
        String reasoning = buildReasoning(agent, eligible.size(), context);

        log.info("Rule-based strategy selected agent {} for order {}", agent.getId(), context.getOrder().getId());

        return RoutingRecommendation.builder()
                .recommendedAgent(agent)
                .confidence(1.0)
                .reasoning(reasoning)
                .strategy(RoutingStrategyType.RULE)
                .fallbackUsed(false)
                .build();
    }

    private String buildReasoning(Agent agent, int eligibleCount, RoutingContext context) {
        return "Agent %s (%s) selected from %d eligible agents with the lowest current load of %d active order(s).%s".formatted(
                agent.getName(),
                agent.getId(),
                eligibleCount,
                agent.getActiveOrderCount(),
                context.isRecoveryMode() ? " Recovery assignment — previous agent went offline." : ""
        );
    }

    @Override
    public String getStrategyKey() {
        return "RULE";
    }
}