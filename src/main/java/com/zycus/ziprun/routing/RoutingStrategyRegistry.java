package com.zycus.ziprun.routing;

import com.zycus.ziprun.common.exception.BadRequestException;
import com.zycus.ziprun.config.RoutingConfiguration;
import com.zycus.ziprun.routing.dto.RoutingContext;
import com.zycus.ziprun.routing.dto.RoutingRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Selects and delegates to the active routing strategy.
 * Spring auto-populates the strategy map by bean name at startup.
 * Startup validation ensures the configured key resolves to a real strategy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingStrategyRegistry {

    private final Map<String, RoutingStrategy> strategies;
    private final RoutingConfiguration routingConfiguration;

    public RoutingRecommendation route(RoutingContext context) {
        String activeKey = routingConfiguration.getActiveStrategy();
        RoutingStrategy strategy = strategies.get(activeKey);

        if (strategy == null) {
            throw new BadRequestException("Routing strategy not found: " + activeKey);
        }

        log.info("Routing order {} using strategy: {}", context.getOrder().getId(), activeKey);
        return strategy.recommend(context);
    }

    public RoutingRecommendation routeWithStrategy(RoutingContext context, String strategyKey) {
        RoutingStrategy strategy = strategies.get(strategyKey.toUpperCase());
        if (strategy == null) {
            throw new BadRequestException("Routing strategy not found: " + strategyKey);
        }
        return strategy.recommend(context);
    }
}