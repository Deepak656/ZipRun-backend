package com.zycus.ziprun.domain.reassignment;

import com.zycus.ziprun.common.enums.*;
import com.zycus.ziprun.common.exception.BadRequestException;
import com.zycus.ziprun.common.exception.ResourceNotFoundException;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.domain.agent.AgentRepository;
import com.zycus.ziprun.domain.order.Order;
import com.zycus.ziprun.domain.order.OrderRepository;
import com.zycus.ziprun.dto.request.GenerateSuggestionRequest;
import com.zycus.ziprun.dto.request.UpdateSuggestionRequest;
import com.zycus.ziprun.dto.response.SuggestionResponse;
import com.zycus.ziprun.mapper.SuggestionMapper;
import com.zycus.ziprun.routing.RoutingStrategyRegistry;
import com.zycus.ziprun.routing.dto.RoutingContext;
import com.zycus.ziprun.routing.dto.RoutingRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReassignmentService {

    private final ReassignmentRepository reassignmentRepository;
    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final RoutingStrategyRegistry routingStrategyRegistry;
    private final SuggestionMapper suggestionMapper;

    /**
     * On-demand suggestion generation — called from the HTTP endpoint.
     * Builds routing context, runs the active strategy, and persists the suggestion.
     */
    @Transactional
    public SuggestionResponse generateSuggestion(GenerateSuggestionRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        List<Agent> availableAgents = agentRepository.findByStatus(AgentStatus.AVAILABLE);

        Agent offlineAgent = null;
        boolean recoveryMode = request.getTriggerReason() == TriggerReason.AGENT_OFFLINE;

        if (recoveryMode) {
            if (request.getOfflineAgentId() == null || request.getOfflineAgentId().isBlank()) {
                throw new BadRequestException("offlineAgentId is required when triggerReason is AGENT_OFFLINE");
            }
            offlineAgent = agentRepository.findById(request.getOfflineAgentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agent", request.getOfflineAgentId()));

            // Exclude the offline agent from candidates
            final String offlineId = request.getOfflineAgentId();
            availableAgents = availableAgents.stream()
                    .filter(a -> !a.getId().equals(offlineId))
                    .toList();
        }

        RoutingContext context = RoutingContext.builder()
                .order(order)
                .availableAgents(availableAgents)
                .triggerReason(request.getTriggerReason())
                .offlineAgent(offlineAgent)
                .recoveryMode(recoveryMode)
                .build();

        RoutingRecommendation recommendation = routingStrategyRegistry.route(context);

        if (recommendation.getRecommendedAgent() == null) {
            throw new BadRequestException("No eligible agent found for order: " + order.getId());
        }

        // Mark order as pending reassignment
        order.markReassignmentPending();
        orderRepository.save(order);

        ReassignmentSuggestion suggestion = buildSuggestion(order, recommendation, request.getTriggerReason());
        ReassignmentSuggestion saved = reassignmentRepository.save(suggestion);

        log.info("Suggestion {} created for order {} -> agent {}",
                saved.getId(), order.getId(), recommendation.getRecommendedAgent().getId());

        return suggestionMapper.toResponse(saved);
    }

    /**
     * Async re-planning path — called by the event listener when an agent goes offline.
     * Idempotent: skips orders that already have a PENDING AGENT_OFFLINE suggestion.
     */
    @Transactional
    public void replanForOfflineAgent(String offlineAgentId) {
        Agent offlineAgent = agentRepository.findById(offlineAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", offlineAgentId));

        List<Order> stranded = orderRepository.findActiveOrdersByAgentId(
                offlineAgentId,
                List.of(OrderStatus.ASSIGNED, OrderStatus.REASSIGNMENT_PENDING)
        );

        log.info("Re-planning for offline agent {}. Found {} stranded order(s).", offlineAgentId, stranded.size());

        List<Agent> availableAgents = agentRepository.findByStatus(AgentStatus.AVAILABLE)
                .stream()
                .filter(a -> !a.getId().equals(offlineAgentId))
                .toList();

        for (Order order : stranded) {
            processStrandedOrder(order, offlineAgent, availableAgents);
        }
    }

    private void processStrandedOrder(Order order, Agent offlineAgent, List<Agent> availableAgents) {
        // Idempotency guard — don't create duplicate PENDING suggestions
        boolean alreadyPending = reassignmentRepository.existsByOrderIdAndTriggerReasonAndStatus(
                order.getId(), TriggerReason.AGENT_OFFLINE, SuggestionStatus.PENDING
        );

        if (alreadyPending) {
            log.info("Skipping order {} — PENDING AGENT_OFFLINE suggestion already exists.", order.getId());
            return;
        }

        RoutingContext context = RoutingContext.builder()
                .order(order)
                .availableAgents(availableAgents)
                .triggerReason(TriggerReason.AGENT_OFFLINE)
                .offlineAgent(offlineAgent)
                .recoveryMode(true)
                .build();

        RoutingRecommendation recommendation;
        try {
            recommendation = routingStrategyRegistry.route(context);
        } catch (Exception e) {
            log.error("Routing failed for stranded order {} during re-plan. Attempting rule-based fallback.", order.getId(), e);
            try {
                recommendation = routingStrategyRegistry.routeWithStrategy(context, "RULE");
            } catch (Exception fallbackEx) {
                log.error("Rule-based fallback also failed for order {}. Skipping suggestion.", order.getId(), fallbackEx);
                return;
            }
        }

        order.markReassignmentPending();
        orderRepository.save(order);

        ReassignmentSuggestion suggestion = buildSuggestion(order, recommendation, TriggerReason.AGENT_OFFLINE);
        reassignmentRepository.save(suggestion);

        log.info("Re-plan suggestion created for stranded order {} -> agent {}",
                order.getId(),
                recommendation.getRecommendedAgent() != null ? recommendation.getRecommendedAgent().getId() : "none");
    }

    /**
     * Ops accepts or rejects a suggestion. On ACCEPT, the order is reassigned
     * to the recommended agent and load counters are updated on both agents.
     */
    @Transactional
    public SuggestionResponse reviewSuggestion(String suggestionId, UpdateSuggestionRequest request) {
        ReassignmentSuggestion suggestion = reassignmentRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion", suggestionId));

        if (!suggestion.isPending()) {
            throw new BadRequestException("Suggestion " + suggestionId + " has already been reviewed.");
        }

        if (request.getDecision() == SuggestionDecision.ACCEPT) {
            applyReassignment(suggestion);
        } else {
            suggestion.reject();
        }

        ReassignmentSuggestion saved = reassignmentRepository.save(suggestion);
        log.info("Suggestion {} {}ED by ops for order {}",
                suggestionId, request.getDecision(), suggestion.getOrder().getId());

        return suggestionMapper.toResponse(saved);
    }

    private void applyReassignment(ReassignmentSuggestion suggestion) {
        Order order = suggestion.getOrder();
        Agent previousAgent = order.getAssignedAgent();
        Agent newAgent = suggestion.getRecommendedAgent();

        // Decrement load from the previous agent if they weren't already offline
        if (previousAgent != null && previousAgent.getStatus() != AgentStatus.OFFLINE) {
            previousAgent.decrementLoad();
            agentRepository.save(previousAgent);
        }

        // Assign to new agent and increment their load
        order.markReassigned(newAgent);
        newAgent.incrementLoad();

        orderRepository.save(order);
        agentRepository.save(newAgent);
        suggestion.accept();
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponse> getSuggestions(SuggestionStatus status) {
        List<ReassignmentSuggestion> suggestions = (status != null)
                ? reassignmentRepository.findByStatus(status)
                : reassignmentRepository.findAll();

        return suggestions.stream()
                .map(suggestionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponse> getSuggestionsForOrder(String orderId) {
        return reassignmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(suggestionMapper::toResponse)
                .toList();
    }

    private ReassignmentSuggestion buildSuggestion(
            Order order,
            RoutingRecommendation recommendation,
            TriggerReason triggerReason) {

        return ReassignmentSuggestion.builder()
                .order(order)
                .recommendedAgent(recommendation.getRecommendedAgent())
                .confidence(recommendation.getConfidence())
                .reasoning(recommendation.getReasoning())
                .strategy(recommendation.getStrategy())
                .triggerReason(triggerReason)
                .status(SuggestionStatus.PENDING)
                .fallbackUsed(recommendation.isFallbackUsed())
                .failureReason(recommendation.getFailureReason())
                .build();
    }
}