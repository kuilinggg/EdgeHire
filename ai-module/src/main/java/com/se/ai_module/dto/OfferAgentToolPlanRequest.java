package com.se.ai_module.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfferAgentToolPlanRequest {
    private String conversationId;
    private String message;
    private String userContext;
    private List<OfferAgentToolDefinition> availableTools = new ArrayList<>();
}
