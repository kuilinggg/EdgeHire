package com.se.ai_module.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.ai_module.dto.OfferAgentLlmJudgeResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OfferAgentEvaluationJudgeParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentLlmJudgeResult parse(String content) {
        OfferAgentLlmJudgeResult result = new OfferAgentLlmJudgeResult();
        result.setRawResponse(content);
        if (content == null || content.isBlank()) {
            return OfferAgentLlmJudgeResult.unavailable("LLM judge returned empty content.");
        }

        try {
            JsonNode root = objectMapper.readTree(extractJson(content));
            if (root.isArray() && !root.isEmpty()) {
                root = root.get(0);
            }

            int score = Math.max(0, Math.min(100, readInt(root, "score", "overallScore", "totalScore")));
            result.setAvailable(true);
            result.setScore(score);
            result.setGrade(readText(root, "grade").isBlank() ? grade(score) : readText(root, "grade"));

            String summary = readText(root, "summary", "comment", "reason");
            result.setSummary(summary.isBlank() ? "LLM 裁判已完成语义质量评估。" : summary);
            result.setStrengths(readStringArray(root, "strengths", "pros", "advantages"));
            result.setRisks(readStringArray(root, "risks", "problems", "issues"));
            result.setSuggestions(readStringArray(root, "suggestions", "advice", "improvements"));
            return result;
        } catch (Exception e) {
            OfferAgentLlmJudgeResult fallback = OfferAgentLlmJudgeResult.unavailable("Failed to parse LLM judge JSON: " + e.getMessage());
            fallback.setRawResponse(content);
            return fallback;
        }
    }

    private int readInt(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = root.path(fieldName);
            if (node.isNumber()) {
                return node.asInt();
            }
            if (node.isTextual()) {
                String digits = node.asText().replaceAll("[^0-9]", "");
                if (!digits.isBlank()) {
                    return Integer.parseInt(digits);
                }
            }
        }
        return 0;
    }

    private String readText(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = root.path(fieldName).asText("");
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private List<String> readStringArray(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            List<String> values = readStringArray(root.path(fieldName));
            if (!values.isEmpty()) {
                return values;
            }
        }
        return List.of();
    }

    private List<String> readStringArray(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return values;
        }
        for (JsonNode item : node) {
            String value = item.asText("");
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private String extractJson(String content) {
        String trimmed = content.trim()
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        int arrayStart = trimmed.indexOf('[');
        int arrayEnd = trimmed.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return trimmed.substring(arrayStart, arrayEnd + 1);
        }
        return trimmed;
    }

    private String grade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "E";
    }
}
