package com.zycus.ziprun.domain.agent;

import com.zycus.ziprun.common.enums.AgentStatus;
import com.zycus.ziprun.common.enums.WeightClass;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "agents",
        indexes = {
                @Index(name = "idx_agent_status", columnList = "status"),
                @Index(name = "idx_agent_zone", columnList = "current_zone")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "supportedWeightClasses")
@EqualsAndHashCode(of = "id")
public class Agent {

    @Id
    @Column(length = 50, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentStatus status;

    /**
     * Number of currently assigned active orders.
     * Updated whenever a reassignment is accepted.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer activeOrderCount = 0;

    // -----------------------------------------------------------------------
    // Sprint 2 (Already part of domain model)
    // -----------------------------------------------------------------------

    /**
     * Current working zone of the agent.
     * Used by ZoneAffinityStrategy in Sprint 2.
     */
    @Column(name = "current_zone", length = 100)
    private String currentZone;

    /**
     * Maximum orders this agent can carry simultaneously.
     */
    @Column(name = "max_capacity")
    private Integer maxCapacity;

    /**
     * Whether this agent can handle LIGHT / HEAVY orders.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "agent_supported_weight_classes",
            joinColumns = @JoinColumn(name = "agent_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "weight_class")
    @Builder.Default
    private Set<WeightClass> supportedWeightClasses = new HashSet<>();

    // -----------------------------------------------------------------------
    // Audit Fields
    // -----------------------------------------------------------------------

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------
    // Lifecycle Hooks
    // -----------------------------------------------------------------------

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (activeOrderCount == null) {
            activeOrderCount = 0;
        }

        if (supportedWeightClasses == null) {
            supportedWeightClasses = new HashSet<>();
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // Domain Methods
    // -----------------------------------------------------------------------

    public boolean isAvailable() {
        return status == AgentStatus.AVAILABLE;
    }

    public boolean hasCapacity() {
        return maxCapacity == null || activeOrderCount < maxCapacity;
    }

    public boolean canHandle(WeightClass weightClass) {
        return supportedWeightClasses.isEmpty()
                || supportedWeightClasses.contains(weightClass);
    }

    public void incrementLoad() {
        this.activeOrderCount++;
    }

    public void decrementLoad() {
        if (this.activeOrderCount > 0) {
            this.activeOrderCount--;
        }
    }

    public void markOffline() {
        this.status = AgentStatus.OFFLINE;
    }

    public void markAvailable() {
        this.status = AgentStatus.AVAILABLE;
    }
}