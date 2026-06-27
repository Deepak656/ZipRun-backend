package com.zycus.ziprun.mapper;

import com.zycus.ziprun.domain.order.Order;
import com.zycus.ziprun.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .assignedAgentId(order.getAssignedAgent() != null ? order.getAssignedAgent().getId() : null)
                .assignedAgentName(order.getAssignedAgent() != null ? order.getAssignedAgent().getName() : null)
                .status(order.getStatus())
                .pickupZone(order.getPickupZone())
                .dropoffZone(order.getDropoffZone())
                .weightClass(order.getWeightClass())
                .priority(order.getPriority())
                .slaDeadline(order.getSlaDeadline())
                .createdAt(order.getCreatedAt())
                .build();
    }
}