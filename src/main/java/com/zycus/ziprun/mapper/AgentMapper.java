package com.zycus.ziprun.mapper;

import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.dto.response.AgentResponse;
import org.springframework.stereotype.Component;

@Component
public class AgentMapper {

    public AgentResponse toResponse(Agent agent) {
        return AgentResponse.builder()
                .id(agent.getId())
                .name(agent.getName())
                .status(agent.getStatus())
                .activeOrderCount(agent.getActiveOrderCount())
                .currentZone(agent.getCurrentZone())
                .maxCapacity(agent.getMaxCapacity())
                .supportedWeightClasses(agent.getSupportedWeightClasses())
                .build();
    }
}