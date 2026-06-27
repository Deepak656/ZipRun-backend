package com.zycus.ziprun.routing;

import com.zycus.ziprun.routing.dto.RoutingContext;
import com.zycus.ziprun.routing.dto.RoutingRecommendation;

/**
 * Contract for all routing strategies. Implementations are registered as
 * Spring beans and keyed by their strategy type name (e.g. "AI", "RULE").
 * The active strategy is selected at call time from application.properties,
 * making it switchable without a restart.
 *
 * Adding a new strategy in Sprint 2 (ZoneAffinityStrategy) means:
 * 1. Implement this interface
 * 2. Annotate with @Component and name the bean "ZONE"
 * No existing code changes required.
 */
public interface RoutingStrategy {

    /**
     * Produces a recommendation for which agent should handle the order
     * described in the given context.
     */
    RoutingRecommendation recommend(RoutingContext context);

    /**
     * Returns the strategy type key used to register this implementation
     * in the bean map. Must match the value used in application.properties.
     */
    String getStrategyKey();
}