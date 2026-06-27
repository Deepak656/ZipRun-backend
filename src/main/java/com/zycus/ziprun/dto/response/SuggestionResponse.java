package com.zycus.ziprun.dto.response;

import com.zycus.ziprun.common.enums.RoutingStrategyType;
import com.zycus.ziprun.common.enums.SuggestionStatus;
import com.zycus.ziprun.common.enums.TriggerReason;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionResponse {

    private String id;

    private String orderId;

    private String orderDescription;

    private String recommendedAgentId;

    private String recommendedAgentName;

    private Double confidence;

    private String reasoning;

    private RoutingStrategyType strategy;

    private TriggerReason triggerReason;

    private SuggestionStatus status;

    private Boolean fallbackUsed;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;
}