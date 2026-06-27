package com.zycus.ziprun.domain.agent;

import com.zycus.ziprun.common.enums.AgentStatus;
import com.zycus.ziprun.common.exception.ResourceNotFoundException;
import com.zycus.ziprun.dto.request.UpdateAgentStatusRequest;
import com.zycus.ziprun.dto.response.AgentResponse;
import com.zycus.ziprun.events.AgentOfflineEvent;
import com.zycus.ziprun.mapper.AgentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<AgentResponse> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(agentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AgentResponse getAgent(String agentId) {
        return agentRepository.findById(agentId)
                .map(agentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getAgentsByStatus(AgentStatus status) {
        return agentRepository.findByStatus(status).stream()
                .map(agentMapper::toResponse)
                .toList();
    }

    /**
     * Updates agent status. If the agent transitions to OFFLINE,
     * publishes an AgentOfflineEvent which triggers async re-planning
     * for all affected orders. The method returns immediately — re-planning
     * is fully decoupled from this transaction.
     */
    @Transactional
    public AgentResponse updateStatus(String agentId, UpdateAgentStatusRequest request) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent", agentId));

        AgentStatus previousStatus = agent.getStatus();
        agent.setStatus(request.getStatus());

        if (request.getStatus() == AgentStatus.OFFLINE) {
            agent.markOffline();
        } else if (request.getStatus() == AgentStatus.AVAILABLE) {
            agent.markAvailable();
        }

        Agent saved = agentRepository.save(agent);
        log.info("Agent {} status changed: {} -> {}", agentId, previousStatus, request.getStatus());

        // Publish AFTER the transaction commits so the event listener
        // sees the updated agent state in its own transaction
        if (request.getStatus() == AgentStatus.OFFLINE && previousStatus != AgentStatus.OFFLINE) {
            eventPublisher.publishEvent(new AgentOfflineEvent(agentId));
            log.info("AgentOfflineEvent published for agent: {}", agentId);
        }

        return agentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<Agent> getAvailableAgents() {
        return agentRepository.findByStatus(AgentStatus.AVAILABLE);
    }
}