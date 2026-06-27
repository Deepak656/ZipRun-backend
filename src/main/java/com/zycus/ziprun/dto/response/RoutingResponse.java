package com.zycus.ziprun.dto.response;

import com.zycus.ziprun.common.enums.RoutingStrategyType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingResponse {

    private String orderId;

    private String recommendedAgentId;

    private String recommendedAgentName;

    private Double confidence;

    private String reasoning;

    private RoutingStrategyType strategy;

    private Boolean fallbackUsed;
}