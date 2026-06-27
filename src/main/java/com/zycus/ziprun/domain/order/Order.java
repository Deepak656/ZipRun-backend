package com.zycus.ziprun.domain.order;

import com.zycus.ziprun.common.enums.OrderStatus;
import com.zycus.ziprun.common.enums.Priority;
import com.zycus.ziprun.common.enums.WeightClass;
import com.zycus.ziprun.domain.agent.Agent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_agent", columnList = "assigned_agent_id"),
                @Index(name = "idx_order_sla", columnList = "sla_deadline"),
                @Index(name = "idx_order_pickup_zone", columnList = "pickup_zone")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "assignedAgent")
public class Order {

    @Id
    @Column(length = 50, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id", nullable = false)
    private Agent assignedAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    // -----------------------------------------------------------------------
    // Sprint 2 Fields
    // -----------------------------------------------------------------------

    @Column(name = "pickup_zone", length = 100)
    private String pickupZone;

    @Column(name = "dropoff_zone", length = 100)
    private String dropoffZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_class", length = 20)
    private WeightClass weightClass;

    // -----------------------------------------------------------------------
    // Sprint 3 Fields
    // -----------------------------------------------------------------------

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    // -----------------------------------------------------------------------
    // Audit
    // -----------------------------------------------------------------------

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------
    // Entity Lifecycle
    // -----------------------------------------------------------------------

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = OrderStatus.ASSIGNED;
        }

        if (priority == null) {
            priority = Priority.NORMAL;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // Domain Methods
    // -----------------------------------------------------------------------

    public void assignTo(Agent agent) {
        this.assignedAgent = agent;
        this.status = OrderStatus.ASSIGNED;
    }

    public void markReassignmentPending() {
        this.status = OrderStatus.REASSIGNMENT_PENDING;
    }

    public void markReassigned(Agent newAgent) {
        this.assignedAgent = newAgent;
        this.status = OrderStatus.REASSIGNED;
    }

    public void markDelivered() {
        this.status = OrderStatus.DELIVERED;
    }

    public boolean isAssigned() {
        return status == OrderStatus.ASSIGNED;
    }

    public boolean isPendingReassignment() {
        return status == OrderStatus.REASSIGNMENT_PENDING;
    }
}