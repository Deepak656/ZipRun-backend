package com.zycus.ziprun.routing.dto;

import com.zycus.ziprun.common.enums.TriggerReason;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.domain.order.Order;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoutingContext {

    private Order order;

    private List<Agent> availableAgents;

    private TriggerReason triggerReason;

    // null for INITIAL routing
    private Agent offlineAgent;

    private boolean recoveryMode;

}
