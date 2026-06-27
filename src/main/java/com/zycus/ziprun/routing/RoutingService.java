package com.zycus.ziprun.routing;

import com.zycus.ziprun.common.enums.AgentStatus;
import com.zycus.ziprun.common.exception.ResourceNotFoundException;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.domain.agent.AgentRepository;
import com.zycus.ziprun.domain.order.Order;
import com.zycus.ziprun.domain.order.OrderRepository;
import com.zycus.ziprun.dto.response.RoutingResponse;
import com.zycus.ziprun.routing.dto.RoutingContext;
import com.zycus.ziprun.routing.dto.RoutingRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Public facade for on-demand routing — wires together domain lookups
 * and strategy execution without persisting a suggestion.
 * Persistence is handled by ReassignmentService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingService {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final RoutingStrategyRegistry strategyRegistry;

    @Transactional(readOnly = true)
    public RoutingResponse getRecommendation(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Agent> available = agentRepository.findByStatus(AgentStatus.AVAILABLE);

        RoutingContext context = RoutingContext.builder()
                .order(order)
                .availableAgents(available)
                .recoveryMode(false)
                .build();

        RoutingRecommendation rec = strategyRegistry.route(context);

        return RoutingResponse.builder()
                .orderId(orderId)
                .recommendedAgentId(rec.getRecommendedAgent() != null ? rec.getRecommendedAgent().getId() : null)
                .recommendedAgentName(rec.getRecommendedAgent() != null ? rec.getRecommendedAgent().getName() : null)
                .confidence(rec.getConfidence())
                .reasoning(rec.getReasoning())
                .strategy(rec.getStrategy())
                .fallbackUsed(rec.isFallbackUsed())
                .build();
    }
}