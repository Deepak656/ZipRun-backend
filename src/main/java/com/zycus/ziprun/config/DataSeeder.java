package com.zycus.ziprun.config;

import com.zycus.ziprun.common.enums.AgentStatus;
import com.zycus.ziprun.common.enums.OrderStatus;
import com.zycus.ziprun.common.enums.Priority;
import com.zycus.ziprun.common.enums.WeightClass;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.domain.agent.AgentRepository;
import com.zycus.ziprun.domain.order.Order;
import com.zycus.ziprun.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds initial demo data on every startup.
 * Runs after Hibernate creates the schema (create-drop), so tables
 * always exist by the time this executes. Safe to run repeatedly
 * because create-drop wipes the database on each restart anyway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AgentRepository agentRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        log.info("=== Seeding demo data ===");

        // ─── Agents ──────────────────────────────────────────

        Agent priya = agentRepository.save(Agent.builder()
                .id("AGT-001")
                .name("Priya Sharma")
                .status(AgentStatus.BUSY)
                .activeOrderCount(2)
                .currentZone("Koramangala")
                .maxCapacity(5)
                .supportedWeightClasses(Set.of(WeightClass.LIGHT, WeightClass.HEAVY))
                .build());

        Agent rahul = agentRepository.save(Agent.builder()
                .id("AGT-002")
                .name("Rahul Verma")
                .status(AgentStatus.AVAILABLE)
                .activeOrderCount(0)
                .currentZone("Indiranagar")
                .maxCapacity(5)
                .supportedWeightClasses(Set.of(WeightClass.LIGHT))
                .build());

        Agent ananya = agentRepository.save(Agent.builder()
                .id("AGT-003")
                .name("Ananya Iyer")
                .status(AgentStatus.BUSY)
                .activeOrderCount(1)
                .currentZone("Whitefield")
                .maxCapacity(5)
                .supportedWeightClasses(Set.of(WeightClass.LIGHT, WeightClass.HEAVY))
                .build());

        Agent kiran = agentRepository.save(Agent.builder()
                .id("AGT-004")
                .name("Kiran Nair")
                .status(AgentStatus.AVAILABLE)
                .activeOrderCount(0)
                .currentZone("MG Road")
                .maxCapacity(5)
                .supportedWeightClasses(Set.of(WeightClass.LIGHT, WeightClass.HEAVY))
                .build());

        Agent deepak = agentRepository.save(Agent.builder()
                .id("AGT-005")
                .name("Deepak Mehta")
                .status(AgentStatus.BUSY)
                .activeOrderCount(3)
                .currentZone("Bellandur")
                .maxCapacity(5)
                .supportedWeightClasses(Set.of(WeightClass.LIGHT, WeightClass.HEAVY))
                .build());

        // ─── Orders ──────────────────────────────────────────

        orderRepository.save(Order.builder()
                .id("ORD-001")
                .description("Electronics — Koramangala to Indiranagar")
                .assignedAgent(priya)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("Koramangala")
                .dropoffZone("Indiranagar")
                .weightClass(WeightClass.LIGHT)
                .priority(Priority.NORMAL)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-002")
                .description("Groceries — HSR Layout to BTM")
                .assignedAgent(priya)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("HSR Layout")
                .dropoffZone("BTM")
                .weightClass(WeightClass.HEAVY)
                .priority(Priority.NORMAL)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-003")
                .description("Pharma — Whitefield to Marathahalli")
                .assignedAgent(ananya)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("Whitefield")
                .dropoffZone("Marathahalli")
                .weightClass(WeightClass.LIGHT)
                .priority(Priority.HIGH)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-004")
                .description("Documents — MG Road to Jayanagar")
                .assignedAgent(deepak)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("MG Road")
                .dropoffZone("Jayanagar")
                .weightClass(WeightClass.LIGHT)
                .priority(Priority.NORMAL)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-005")
                .description("Food — Bellandur to Electronic City")
                .assignedAgent(deepak)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("Bellandur")
                .dropoffZone("Electronic City")
                .weightClass(WeightClass.LIGHT)
                .priority(Priority.HIGH)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-006")
                .description("Apparel — Malleshwaram to Rajajinagar")
                .assignedAgent(deepak)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("Malleshwaram")
                .dropoffZone("Rajajinagar")
                .weightClass(WeightClass.HEAVY)
                .priority(Priority.NORMAL)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-007")
                .description("Books — Banashankari to JP Nagar")
                .assignedAgent(ananya)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("Banashankari")
                .dropoffZone("JP Nagar")
                .weightClass(WeightClass.LIGHT)
                .priority(Priority.LOW)
                .build());

        orderRepository.save(Order.builder()
                .id("ORD-008")
                .description("Hardware — Peenya to Yeshwanthpur")
                .assignedAgent(priya)
                .status(OrderStatus.ASSIGNED)
                .pickupZone("Peenya")
                .dropoffZone("Yeshwanthpur")
                .weightClass(WeightClass.HEAVY)
                .priority(Priority.PREMIUM)
                .build());

        log.info("=== Seeding complete — 5 agents, 8 orders ===");
    }
}