package com.zycus.ziprun.domain.reassignment;

import com.zycus.ziprun.common.enums.SuggestionStatus;
import com.zycus.ziprun.common.enums.TriggerReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReassignmentRepository extends JpaRepository<ReassignmentSuggestion, String> {

    List<ReassignmentSuggestion> findByStatus(SuggestionStatus status);

    List<ReassignmentSuggestion> findByOrderIdOrderByCreatedAtDesc(String orderId);

    /**
     * Used for idempotency check — prevents duplicate AGENT_OFFLINE suggestions
     * for the same order when the same agent goes offline more than once.
     */
    Optional<ReassignmentSuggestion> findByOrderIdAndTriggerReasonAndStatus(
            String orderId,
            TriggerReason triggerReason,
            SuggestionStatus status
    );

    boolean existsByOrderIdAndTriggerReasonAndStatus(
            String orderId,
            TriggerReason triggerReason,
            SuggestionStatus status
    );
}