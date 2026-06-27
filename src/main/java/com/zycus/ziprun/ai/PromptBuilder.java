package com.zycus.ziprun.ai;

import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.routing.dto.RoutingContext;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    /**
     * Builds the appropriate prompt based on whether this is an initial
     * assignment or a recovery re-plan triggered by an agent going offline.
     * These are intentionally different documents — the model needs different
     * context to reason well in each situation.
     */
    public String buildPrompt(RoutingContext context, PromptType promptType) {
        return switch (promptType) {
            case INITIAL  -> buildInitialPrompt(context);
            case RECOVERY -> buildRecoveryPrompt(context);
        };
    }

    private String buildInitialPrompt(RoutingContext context) {
        return """
                You are a logistics routing engine. Your job is to assign a delivery order to the most suitable available agent.
                
                ## Order Details
                - Order ID: %s
                - Description: %s
                - Pickup Zone: %s
                - Dropoff Zone: %s
                - Weight Class: %s
                - Priority: %s
                
                ## Available Agents
                %s
                
                ## Task
                Select the single best agent from the list above. Consider:
                1. Agent current load (prefer agents with fewer active orders)
                2. Agent capacity (do not assign to agents at max capacity)
                3. Weight class compatibility
                4. Zone proximity if applicable
                
                ## Required Output Format
                Respond with ONLY a valid JSON object — no explanation, no markdown, no code fences:
                {"agentId":"<agent_id>","confidence":<0.0_to_1.0>,"reasoning":"<plain English explanation for ops team, 1-2 sentences>"}
                """.formatted(
                context.getOrder().getId(),
                context.getOrder().getDescription(),
                context.getOrder().getPickupZone(),
                context.getOrder().getDropoffZone(),
                context.getOrder().getWeightClass(),
                context.getOrder().getPriority(),
                formatAgentList(context)
        );
    }

    private String buildRecoveryPrompt(RoutingContext context) {
        String offlineAgentName = context.getOfflineAgent() != null
                ? context.getOfflineAgent().getName() + " (" + context.getOfflineAgent().getId() + ")"
                : "Unknown";

        return """
                You are a logistics routing engine in RECOVERY MODE. An agent has gone offline mid-shift, and their orders need to be urgently reassigned.
                
                ## Situation
                - Agent who went offline: %s
                - This is a recovery re-plan, not a fresh assignment
                - The order below was previously assigned to the offline agent and is now stranded
                - All prior assignments to the offline agent are void
                
                ## Stranded Order
                - Order ID: %s
                - Description: %s
                - Pickup Zone: %s
                - Dropoff Zone: %s
                - Weight Class: %s
                - Priority: %s
                
                ## Available Agents (excluding the offline agent)
                %s
                
                ## Task
                Select the single best available agent to take over this stranded order. In recovery mode, prioritize:
                1. Agent availability and remaining capacity
                2. Minimising further disruption — avoid piling too many orders on one agent
                3. Weight class compatibility
                4. Zone proximity if there are multiple suitable agents
                
                ## Required Output Format
                Respond with ONLY a valid JSON object — no explanation, no markdown, no code fences:
                {"agentId":"<agent_id>","confidence":<0.0_to_1.0>,"reasoning":"<plain English explanation for ops team mentioning the recovery context, 1-2 sentences>"}
                """.formatted(
                offlineAgentName,
                context.getOrder().getId(),
                context.getOrder().getDescription(),
                context.getOrder().getPickupZone(),
                context.getOrder().getDropoffZone(),
                context.getOrder().getWeightClass(),
                context.getOrder().getPriority(),
                formatAgentList(context)
        );
    }

    private String formatAgentList(RoutingContext context) {
        if (context.getAvailableAgents() == null || context.getAvailableAgents().isEmpty()) {
            return "No agents currently available.";
        }

        return context.getAvailableAgents().stream()
                .map(agent -> "- ID: %s | Name: %s | Status: %s | Active Orders: %d | Max Capacity: %s | Zone: %s | Supported Weight: %s".formatted(
                        agent.getId(),
                        agent.getName(),
                        agent.getStatus(),
                        agent.getActiveOrderCount(),
                        agent.getMaxCapacity() != null ? agent.getMaxCapacity().toString() : "unlimited",
                        agent.getCurrentZone() != null ? agent.getCurrentZone() : "unknown",
                        agent.getSupportedWeightClasses().isEmpty() ? "ALL" : agent.getSupportedWeightClasses().toString()
                ))
                .collect(Collectors.joining("\n"));
    }
}