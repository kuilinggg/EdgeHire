package com.se.ai_module.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.ai_module.dto.OfferAgentPlannedToolCall;
import com.se.ai_module.dto.OfferAgentToolPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OfferAgentToolPlanParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentToolPlan parse(String content) {
        OfferAgentToolPlan plan = new OfferAgentToolPlan();
        plan.setSource("llm");
        if (content == null || content.isBlank()) {
            return plan;
        }

        try {
            JsonNode root = objectMapper.readTree(extractJson(content));
            JsonNode callsNode = root.path("toolCalls");
            if (!callsNode.isArray()) {
                return plan;
            }

            List<OfferAgentPlannedToolCall> calls = new ArrayList<>();
            for (JsonNode item : callsNode) {
                String toolName = item.path("toolName").asText("");
                if (toolName.isBlank()) {
                    continue;
                }
                OfferAgentPlannedToolCall call = new OfferAgentPlannedToolCall();
                call.setToolName(toolName);
                call.setArguments(readArguments(item.path("arguments")));
                calls.add(call);
            }
            plan.setToolCalls(calls);
            return plan;
        } catch (Exception ignored) {
            return plan;
        }
    }

    private Map<String, Object> readArguments(JsonNode argumentsNode) {
        if (argumentsNode == null || !argumentsNode.isObject()) {
            return new LinkedHashMap<>();
        }
        return objectMapper.convertValue(argumentsNode, objectMapper.getTypeFactory()
                .constructMapType(LinkedHashMap.class, String.class, Object.class));
    }

    private String extractJson(String content) {
        String trimmed = content.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
