package com.se.EdgeHire.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OfferAgentToolResult {
    private String toolName;
    private String description;
    private String inputJson;
    private String outputJson;
    private String summary;
    private Boolean success;
    private String errorMessage;
    private String planSource;

    public OfferAgentToolResult(
            String toolName,
            String description,
            String inputJson,
            String outputJson,
            String summary,
            Boolean success,
            String errorMessage) {
        this(toolName, description, inputJson, outputJson, summary, success, errorMessage, "rule_fallback");
    }

    public OfferAgentToolResult(
            String toolName,
            String description,
            String inputJson,
            String outputJson,
            String summary,
            Boolean success,
            String errorMessage,
            String planSource) {
        this.toolName = toolName;
        this.description = description;
        this.inputJson = inputJson;
        this.outputJson = outputJson;
        this.summary = summary;
        this.success = success;
        this.errorMessage = errorMessage;
        this.planSource = planSource;
    }
}
