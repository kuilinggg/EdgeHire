package com.se.ai_module.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.ai_module.dto.OfferAgentToolPlanRequest;
import org.springframework.stereotype.Component;

@Component
public class OfferAgentToolPlanPromptBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String build(OfferAgentToolPlanRequest request) {
        return """
                You are the tool planner for OfferAgent.

                Task:
                Select the smallest useful set of tools for the user's latest job-search question.

                Rules:
                - Return JSON only. Do not use markdown.
                - The JSON schema is: {"toolCalls":[{"toolName":"string","arguments":{}}]}
                - Use only toolName values from Available Tools.
                - Prefer 2 to 5 tools. Never return more than 6 tools.
                - Use get_user_profile, get_job_intention, and get_resume_summary when the answer needs personal context.
                - Use retrieve_knowledge when the answer needs job-search, resume, interview, RAG, or AI Agent knowledge.
                - Use calculate_job_match_score when the user asks about match, fit, gap, score, suitable roles, or target position comparison.
                - Use generate_interview_plan when the user asks about interview preparation, mock interview, questions, or preparation plan.
                - Put natural search text in retrieve_knowledge.arguments.query.
                - Put target role in targetPosition when you can infer it.

                Available Tools:
                %s

                User Context:
                %s

                User Message:
                %s
                """.formatted(toJson(request.getAvailableTools()), safe(request.getUserContext()), safe(request.getMessage()));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
