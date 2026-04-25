package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentWorkflowStep {
    private String agentName;
    private String status;
    private String summary;
    private List<OfferAgentToolResult> toolCalls = new ArrayList<>();
}
