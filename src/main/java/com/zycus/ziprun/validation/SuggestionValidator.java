package com.zycus.ziprun.validation;

import com.zycus.ziprun.common.exception.BadRequestException;
import com.zycus.ziprun.domain.reassignment.ReassignmentSuggestion;
import org.springframework.stereotype.Component;

@Component
public class SuggestionValidator {

    public void validatePending(ReassignmentSuggestion suggestion) {
        if (!suggestion.isPending()) {
            throw new BadRequestException(
                    "Suggestion " + suggestion.getId() + " has already been reviewed with status: " + suggestion.getStatus());
        }
    }
}