package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolPlanRequest {
    private String conversationId;
    private String message;
    private String userContext;
    private List<OfferAgentToolDefinition> availableTools = new ArrayList<>();
}
