package com.zycus.ziprun.events;

import com.zycus.ziprun.common.enums.TriggerReason;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor
public class RoutingTriggerEvent {

    private String orderId;

    private String agentId;

    private TriggerReason triggerReason;

}