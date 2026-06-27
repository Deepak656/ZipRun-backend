package com.zycus.ziprun.events;

import com.zycus.ziprun.common.enums.AgentStatus;
import lombok.Getter;

@Getter
public class AgentStatusChangedEvent {

    private final String agentId;
    private final AgentStatus previousStatus;
    private final AgentStatus newStatus;

    public AgentStatusChangedEvent(String agentId, AgentStatus previousStatus, AgentStatus newStatus) {
        this.agentId        = agentId;
        this.previousStatus = previousStatus;
        this.newStatus      = newStatus;
    }
}