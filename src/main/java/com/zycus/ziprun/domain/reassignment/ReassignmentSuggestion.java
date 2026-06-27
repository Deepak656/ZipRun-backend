package com.zycus.ziprun.domain.reassignment;

import com.zycus.ziprun.common.enums.RoutingStrategyType;
import com.zycus.ziprun.common.enums.SuggestionStatus;
import com.zycus.ziprun.common.enums.TriggerReason;
import com.zycus.ziprun.domain.agent.Agent;
import com.zycus.ziprun.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "reassignment_suggestions",
        indexes = {
                @Index(name = "idx_rs_order", columnList = "order_id"),
                @Index(name = "idx_rs_status", columnList = "status"),
                @Index(name = "idx_rs_trigger", columnList = "trigger_reason"),
                @Index(name = "idx_rs_agent", columnList = "recommended_agent_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"order", "recommendedAgent"})
public class ReassignmentSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Order requiring reassignment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Agent recommended by Rule Engine / AI.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommended_agent_id", nullable = false)
    private Agent recommendedAgent;

    /**
     * AI confidence.
     * Rule strategy always returns 1.0
     */
    @Column(nullable = false)
    private Double confidence;

    /**
     * Human-readable explanation shown in UI.
     */
    @Lob
    @Column(nullable = false)
    private String reasoning;

    /**
     * AI or RULE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoutingStrategyType strategy;

    /**
     * INITIAL / AGENT_OFFLINE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_reason", nullable = false, length = 30)
    private TriggerReason triggerReason;

    /**
     * PENDING / ACCEPTED / REJECTED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SuggestionStatus status;

    /**
     * Whether this suggestion was created using
     * rule fallback because AI failed.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean fallbackUsed = false;

    /**
     * Optional failure reason for diagnostics.
     */
    @Column(length = 1000)
    private String failureReason;

    // -----------------------------------------------------------------------
    // Audit
    // -----------------------------------------------------------------------

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime reviewedAt;

    // -----------------------------------------------------------------------
    // Entity Lifecycle
    // -----------------------------------------------------------------------

    @PrePersist
    public void onCreate() {

        createdAt = LocalDateTime.now();

        if (status == null) {
            status = SuggestionStatus.PENDING;
        }

        if (confidence == null) {
            confidence = 1.0;
        }

        if (fallbackUsed == null) {
            fallbackUsed = false;
        }
    }

    // -----------------------------------------------------------------------
    // Domain Methods
    // -----------------------------------------------------------------------

    public void accept() {
        this.status = SuggestionStatus.ACCEPTED;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = SuggestionStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == SuggestionStatus.PENDING;
    }

    public boolean isAgenticSuggestion() {
        return triggerReason == TriggerReason.AGENT_OFFLINE;
    }

    public boolean isFallbackSuggestion() {
        return Boolean.TRUE.equals(fallbackUsed);
    }
}