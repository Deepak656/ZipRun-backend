package com.zycus.ziprun.domain.agent;

import com.zycus.ziprun.common.enums.AgentStatus;
import com.zycus.ziprun.common.response.ApiResponse;
import com.zycus.ziprun.dto.request.UpdateAgentStatusRequest;
import com.zycus.ziprun.dto.response.AgentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgentResponse>>> getAllAgents(
            @RequestParam(required = false) AgentStatus status) {

        List<AgentResponse> agents = (status != null)
                ? agentService.getAgentsByStatus(status)
                : agentService.getAllAgents();

        return ResponseEntity.ok(ApiResponse.<List<AgentResponse>>builder()
                .success(true)
                .message("Agents retrieved successfully")
                .data(agents)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<ApiResponse<AgentResponse>> getAgent(@PathVariable String agentId) {
        return ResponseEntity.ok(ApiResponse.<AgentResponse>builder()
                .success(true)
                .message("Agent retrieved successfully")
                .data(agentService.getAgent(agentId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/{agentId}/status")
    public ResponseEntity<ApiResponse<AgentResponse>> updateStatus(
            @PathVariable String agentId,
            @Valid @RequestBody UpdateAgentStatusRequest request) {

        AgentResponse updated = agentService.updateStatus(agentId, request);

        return ResponseEntity.ok(ApiResponse.<AgentResponse>builder()
                .success(true)
                .message("Agent status updated to " + request.getStatus())
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build());
    }
}