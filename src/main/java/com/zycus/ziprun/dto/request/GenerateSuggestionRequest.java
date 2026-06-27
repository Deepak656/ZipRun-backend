package com.zycus.ziprun.dto.request;

import com.zycus.ziprun.common.enums.TriggerReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSuggestionRequest {

    @NotBlank(message = "Order id is required")
    private String orderId;

    @NotNull(message = "Trigger reason is required")
    private TriggerReason triggerReason;

    /**
     * Required only when TriggerReason = AGENT_OFFLINE
     */
    private String offlineAgentId;
}