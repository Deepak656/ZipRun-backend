package com.zycus.ziprun.dto.response;

import com.zycus.ziprun.common.enums.AgentStatus;
import com.zycus.ziprun.common.enums.WeightClass;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    private String id;

    private String name;

    private AgentStatus status;

    private Integer activeOrderCount;

    // Sprint 2
    private String currentZone;

    private Integer maxCapacity;

    private Set<WeightClass> supportedWeightClasses;
}