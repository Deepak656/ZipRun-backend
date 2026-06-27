package com.zycus.ziprun.events;

import com.zycus.ziprun.common.enums.TriggerReason;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishAgentOffline(String agentId) {
        publisher.publishEvent(new AgentOfflineEvent(agentId));
    }

    public void publishRoutingTrigger(
            String orderId,
            String agentId,
            TriggerReason triggerReason
    ) {
        publisher.publishEvent(
                RoutingTriggerEvent.builder()
                        .orderId(orderId)
                        .agentId(agentId)
                        .triggerReason(triggerReason)
                        .build()
        );
    }
}