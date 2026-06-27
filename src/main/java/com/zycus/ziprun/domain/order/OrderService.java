package com.zycus.ziprun.domain.order;

import com.zycus.ziprun.common.enums.OrderStatus;
import com.zycus.ziprun.common.exception.BadRequestException;
import com.zycus.ziprun.common.exception.ResourceNotFoundException;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.domain.agent.AgentRepository;
import com.zycus.ziprun.dto.request.CreateOrderRequest;
import com.zycus.ziprun.dto.response.OrderResponse;
import com.zycus.ziprun.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Agent agent = agentRepository.findById(request.getAssignedAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", request.getAssignedAgentId()));

        if (!agent.hasCapacity()) {
            throw new BadRequestException(
                    "Agent " + agent.getId() + " is at maximum capacity and cannot accept new orders.");
        }

        Order order = Order.builder()
                .id("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .description(request.getDescription())
                .assignedAgent(agent)
                .status(OrderStatus.ASSIGNED)
                .build();

        agent.incrementLoad();
        agentRepository.save(agent);

        Order saved = orderRepository.save(order);
        log.info("Order {} created and assigned to agent {}", saved.getId(), agent.getId());

        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(OrderStatus status) {
        List<Order> orders = (status != null)
                ? orderRepository.findByStatus(status)
                : orderRepository.findAll();

        return orders.stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    @Transactional
    public OrderResponse markDelivered(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        order.markDelivered();
        order.getAssignedAgent().decrementLoad();
        agentRepository.save(order.getAssignedAgent());

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<Order> findOrdersByAgentAndStatus(String agentId, List<OrderStatus> statuses) {
        return orderRepository.findActiveOrdersByAgentId(agentId, statuses);
    }
}