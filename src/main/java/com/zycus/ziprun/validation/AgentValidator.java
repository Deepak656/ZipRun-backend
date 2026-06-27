package com.zycus.ziprun.validation;

import com.zycus.ziprun.common.exception.BadRequestException;
import com.zycus.ziprun.domain.agent.Agent;
import org.springframework.stereotype.Component;

@Component
public class AgentValidator {

    public void validateCanAcceptOrder(Agent agent) {
        if (!agent.isAvailable()) {
            throw new BadRequestException(
                    "Agent " + agent.getId() + " is not available (current status: " + agent.getStatus() + ")");
        }
        if (!agent.hasCapacity()) {
            throw new BadRequestException(
                    "Agent " + agent.getId() + " has reached maximum capacity of " + agent.getMaxCapacity() + " orders");
        }
    }
}