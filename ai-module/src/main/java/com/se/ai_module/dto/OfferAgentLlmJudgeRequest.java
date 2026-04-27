package com.se.ai_module.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class OfferAgentLlmJudgeRequest {
    private String conversationId;
    private String message;
    private String targetPosition;
    private String finalAnswer;
    private Integer ruleScore;
    private String ruleGrade;
    private List<Map<String, Object>> ruleMetrics = new ArrayList<>();
    private List<String> toolTrace = new ArrayList<>();
    private List<Map<String, Object>> toolCalls = new ArrayList<>();
}
