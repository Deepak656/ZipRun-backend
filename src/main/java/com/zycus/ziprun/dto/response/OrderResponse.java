package com.zycus.ziprun.dto.response;

import com.zycus.ziprun.common.enums.OrderStatus;
import com.zycus.ziprun.common.enums.Priority;
import com.zycus.ziprun.common.enums.WeightClass;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;

    private String description;

    private String assignedAgentId;

    private String assignedAgentName;

    private OrderStatus status;

    // Sprint 2

    private String pickupZone;

    private String dropoffZone;

    private WeightClass weightClass;

    // Sprint 3

    private Priority priority;

    private LocalDateTime slaDeadline;

    private LocalDateTime createdAt;
}