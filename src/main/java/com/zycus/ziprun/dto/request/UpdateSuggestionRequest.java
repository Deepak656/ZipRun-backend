package com.zycus.ziprun.dto.request;

import com.zycus.ziprun.common.enums.SuggestionDecision;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSuggestionRequest {

    /**
     * Only ACCEPT or REJECT are allowed.
     * Validation will be performed in SuggestionValidator.
     */
    @NotNull(message = "Suggestion decision is required")
    private SuggestionDecision decision;
}