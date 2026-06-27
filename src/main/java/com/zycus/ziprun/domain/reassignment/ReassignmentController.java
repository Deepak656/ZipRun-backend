package com.zycus.ziprun.domain.reassignment;

import com.zycus.ziprun.common.enums.SuggestionStatus;
import com.zycus.ziprun.common.response.ApiResponse;
import com.zycus.ziprun.dto.request.GenerateSuggestionRequest;
import com.zycus.ziprun.dto.request.UpdateSuggestionRequest;
import com.zycus.ziprun.dto.response.SuggestionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReassignmentController {

    private final ReassignmentService reassignmentService;

    /**
     * On-demand suggestion — ops or integration triggers this manually.
     * The agentic loop (T-4) creates suggestions automatically via event listener.
     */
    @PostMapping("/orders/{orderId}/suggest")
    public ResponseEntity<ApiResponse<SuggestionResponse>> generateSuggestion(
            @PathVariable String orderId,
            @Valid @RequestBody GenerateSuggestionRequest request) {

        request = GenerateSuggestionRequest.builder()
                .orderId(orderId)
                .triggerReason(request.getTriggerReason())
                .offlineAgentId(request.getOfflineAgentId())
                .build();

        SuggestionResponse suggestion = reassignmentService.generateSuggestion(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<SuggestionResponse>builder()
                        .success(true)
                        .message("Reassignment suggestion generated")
                        .data(suggestion)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SuggestionResponse>>> getSuggestions(
            @RequestParam(required = false) SuggestionStatus status) {

        return ResponseEntity.ok(ApiResponse.<List<SuggestionResponse>>builder()
                .success(true)
                .message("Suggestions retrieved successfully")
                .data(reassignmentService.getSuggestions(status))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/orders/{orderId}/suggestions")
    public ResponseEntity<ApiResponse<List<SuggestionResponse>>> getSuggestionsForOrder(
            @PathVariable String orderId) {

        return ResponseEntity.ok(ApiResponse.<List<SuggestionResponse>>builder()
                .success(true)
                .message("Suggestions for order " + orderId)
                .data(reassignmentService.getSuggestionsForOrder(orderId))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/suggestions/{suggestionId}")
    public ResponseEntity<ApiResponse<SuggestionResponse>> reviewSuggestion(
            @PathVariable String suggestionId,
            @Valid @RequestBody UpdateSuggestionRequest request) {

        SuggestionResponse updated = reassignmentService.reviewSuggestion(suggestionId, request);

        return ResponseEntity.ok(ApiResponse.<SuggestionResponse>builder()
                .success(true)
                .message("Suggestion " + request.getDecision().name().toLowerCase() + "ed")
                .data(updated)
                .timestamp(LocalDateTime.now())
                .build());
    }
}