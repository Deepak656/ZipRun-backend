package com.zycus.ziprun.validation;

import com.zycus.ziprun.ai.AIResponse;
import com.zycus.ziprun.domain.agent.Agent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AIResponseValidator {

    public boolean isValid(AIResponse response, List<Agent> availableAgents) {
        if (response == null) {
            log.warn("AI response is null");
            return false;
        }

        if (response.getAgentId() == null || response.getAgentId().isBlank()) {
            log.warn("AI response has blank agentId");
            return false;
        }

        if (response.getConfidence() == null
                || response.getConfidence() < 0.0
                || response.getConfidence() > 1.0) {
            log.warn("AI response has invalid confidence: {}", response.getConfidence());
            return false;
        }

        Set<String> validIds = availableAgents.stream()
                .map(Agent::getId)
                .collect(Collectors.toSet());

        if (!validIds.contains(response.getAgentId())) {
            log.warn("AI response references unknown agentId: {}. Valid IDs: {}", response.getAgentId(), validIds);
            return false;
        }

        return true;
    }
}