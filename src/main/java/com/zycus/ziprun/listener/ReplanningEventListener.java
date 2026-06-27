package com.zycus.ziprun.listener;

import com.zycus.ziprun.domain.reassignment.ReassignmentService;
import com.zycus.ziprun.events.AgentOfflineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for AgentOfflineEvent and triggers the async re-planning loop.
 *
 * The @Async annotation ensures this runs on the replanningExecutor thread pool,
 * completely off the HTTP request path. The PATCH /agents/{id}/status endpoint
 * that publishes this event returns to the caller before this method even starts.
 *
 * Error propagation: any unhandled exception here is logged but does not
 * surface to the original caller — by design. Re-planning failures are
 * operational concerns, not HTTP response concerns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplanningEventListener {

    private final ReassignmentService reassignmentService;

    @Async("replanningExecutor")
    @EventListener
    public void onAgentOffline(AgentOfflineEvent event) {
        log.info("[AGENTIC LOOP] Agent {} went offline. Starting re-planning.", event.getAgentId());

        try {
            reassignmentService.replanForOfflineAgent(event.getAgentId());
            log.info("[AGENTIC LOOP] Re-planning complete for agent {}.", event.getAgentId());
        } catch (Exception e) {
            log.error("[AGENTIC LOOP] Re-planning failed for agent {}. Manual intervention may be required. Error: {}",
                    event.getAgentId(), e.getMessage(), e);
        }
    }
}