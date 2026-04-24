package com.se.ai_module.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfferAgentToolPlan {
    private String source = "llm";
    private String fallbackReason;
    private List<OfferAgentPlannedToolCall> toolCalls = new ArrayList<>();
}
