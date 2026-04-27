package com.se.EdgeHire.DTO;

import lombok.Data;

@Data
public class OfferAgentEvaluationRequest {
    private Integer userId;
    private String conversationId;
    private String message;
    private String targetPosition;
    private String finalAnswer;
    private OfferAgentToolExecutionReport toolReport;
}
