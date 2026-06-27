package com.zycus.ziprun.ai;


import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse {

    private String agentId;

    private Double confidence;

    private String reasoning;

}
