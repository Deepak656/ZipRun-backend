package com.zycus.ziprun.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "Order description is required")
    private String description;

    @NotBlank(message = "Assigned agent id is required")
    private String assignedAgentId;
}