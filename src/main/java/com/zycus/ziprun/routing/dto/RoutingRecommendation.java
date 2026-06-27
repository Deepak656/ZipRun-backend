package com.zycus.ziprun.routing.dto;

import com.zycus.ziprun.common.enums.RoutingStrategyType;
import com.zycus.ziprun.domain.agent.Agent;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRecommendation {

    private Agent recommendedAgent;

    private Double confidence;

    private String reasoning;

    private RoutingStrategyType strategy;

    private boolean fallbackUsed;

    /**
     * Populated when fallbackUsed = true to aid diagnostics and ADR transparency.
     */
    private String failureReason;
}