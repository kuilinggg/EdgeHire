package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentWorkflowStreamEvent {
    private String type;
    private String workflowName;
    private String targetPosition;
    private String conversationId;
    private String agentName;
    private OfferAgentWorkflowStep step;
    private String content;
    private String message;
    private Boolean done;
}
