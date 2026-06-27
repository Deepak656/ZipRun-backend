package com.zycus.ziprun.events;

import lombok.Getter;

@Getter
public class AgentOfflineEvent {

    private final String agentId;

    public AgentOfflineEvent(String agentId) {
        this.agentId = agentId;
    }
}