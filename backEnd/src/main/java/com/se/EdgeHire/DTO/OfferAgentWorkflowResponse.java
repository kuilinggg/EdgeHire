package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentWorkflowResponse {
    private String workflowName;
    private String targetPosition;
    private String conversationId;
    private List<OfferAgentWorkflowStep> steps = new ArrayList<>();
    private String finalReport;
}
