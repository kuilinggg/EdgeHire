package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentLlmJudgeRequest {
    private String conversationId;
    private String message;
    private String targetPosition;
    private String finalAnswer;
    private Integer ruleScore;
    private String ruleGrade;
    private List<OfferAgentEvaluationMetric> ruleMetrics = new ArrayList<>();
    private List<String> toolTrace = new ArrayList<>();
    private List<OfferAgentToolResult> toolCalls = new ArrayList<>();
}
