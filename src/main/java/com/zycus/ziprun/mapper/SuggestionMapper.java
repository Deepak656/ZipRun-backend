package com.zycus.ziprun.mapper;

import com.zycus.ziprun.domain.reassignment.ReassignmentSuggestion;
import com.zycus.ziprun.dto.response.SuggestionResponse;
import org.springframework.stereotype.Component;

@Component
public class SuggestionMapper {

    public SuggestionResponse toResponse(ReassignmentSuggestion suggestion) {
        return SuggestionResponse.builder()
                .id(suggestion.getId())
                .orderId(suggestion.getOrder().getId())
                .orderDescription(suggestion.getOrder().getDescription())
                .recommendedAgentId(suggestion.getRecommendedAgent().getId())
                .recommendedAgentName(suggestion.getRecommendedAgent().getName())
                .confidence(suggestion.getConfidence())
                .reasoning(suggestion.getReasoning())
                .strategy(suggestion.getStrategy())
                .triggerReason(suggestion.getTriggerReason())
                .status(suggestion.getStatus())
                .fallbackUsed(suggestion.getFallbackUsed())
                .createdAt(suggestion.getCreatedAt())
                .reviewedAt(suggestion.getReviewedAt())
                .build();
    }
}