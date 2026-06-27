package com.zycus.ziprun.dto.request;

import com.zycus.ziprun.common.enums.AgentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAgentStatusRequest {

    @NotNull(message = "Agent status is required")
    private AgentStatus status;
}