package com.se.EdgeHire.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentMemoryResponse;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.DTO.OfferAgentWorkflowStep;
import com.se.EdgeHire.Entity.OfferAgentMemory;
import com.se.EdgeHire.Repository.OfferAgentMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferAgentMemoryService {
    private static final int SUMMARY_LIMIT = 500;
    private static final List<String> KNOWN_SKILLS = List.of(
            "Java", "Spring Boot", "MySQL", "Redis", "Vue", "React", "TypeScript",
            "JavaScript", "Python", "RAG", "Embedding", "VectorStore", "Tool Calling",
            "AI Agent", "Prompt", "JUnit", "Element Plus", "Vite"
    );

    private final OfferAgentMemoryRepository memoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentMemoryResponse findByUserId(Integer userId) {
        try {
            return memoryRepository.findByUserId(userId)
                    .map(this::toResponse)
                    .orElseGet(() -> empty(userId));
        } catch (DataAccessException ignored) {
            return empty(userId);
        }
    }

    public String toPromptMemory(Integer userId) {
        OfferAgentMemoryResponse memory = findByUserId(userId);
        if (memory.getLastInteractionAt() == null) {
            return "- No long-term memory found.\n";
        }
        StringBuilder builder = new StringBuilder();
        append(builder, "targetPosition", memory.getTargetPosition());
        append(builder, "profileSummary", memory.getProfileSummary());
        append(builder, "skillTags", String.join(", ", memory.getSkillTags()));
        append(builder, "gapTags", String.join(", ", memory.getGapTags()));
        append(builder, "preferenceTags", String.join(", ", memory.getPreferenceTags()));
        append(builder, "suggestionSummary", memory.getSuggestionSummary());
        append(builder, "evidenceSummary", memory.getEvidenceSummary());
        append(builder, "source", memory.getSource());
        append(builder, "lastInteractionAt", memory.getLastInteractionAt());
        return builder.toString();
    }

    public void updateFromInteraction(
            Integer userId,
            String message,
            String finalAnswer,
            OfferAgentToolExecutionReport report) {
        List<OfferAgentToolResult> toolCalls = report == null || report.getToolCalls() == null
                ? List.of()
                : report.getToolCalls();
        update(userId, message, finalAnswer, toolCalls, "chat");
    }

    public void updateFromWorkflow(
            Integer userId,
            String targetPosition,
            String finalReport,
            List<OfferAgentWorkflowStep> steps) {
        List<OfferAgentToolResult> toolCalls = steps == null
                ? List.of()
                : steps.stream()
                        .filter(step -> step.getToolCalls() != null)
                        .flatMap(step -> step.getToolCalls().stream())
                        .toList();
        update(userId, targetPosition, finalReport, toolCalls, "workflow");
    }

    private void update(
            Integer userId,
            String userSignal,
            String generatedText,
            List<OfferAgentToolResult> toolCalls,
            String source) {
        if (userId == null) {
            return;
        }
        try {
            OfferAgentMemory memory = memoryRepository.findByUserId(userId).orElseGet(() -> {
                OfferAgentMemory created = new OfferAgentMemory();
                created.setUserId(userId);
                created.setCreatedAt(LocalDateTime.now());
                return created;
            });
            OfferAgentMemoryResponse old = toResponse(memory);

            List<String> skills = merge(old.getSkillTags(), extractSkills(generatedText), extractToolArray(toolCalls, "detectedSkills"));
            List<String> gaps = merge(old.getGapTags(), extractToolArray(toolCalls, "missingKeywords"), extractGaps(generatedText));
            List<String> preferences = merge(old.getPreferenceTags(), extractTargetCandidates(userSignal), extractToolArray(toolCalls, "targetPositions"));
            String targetPosition = firstNonBlank(
                    first(preferences),
                    old.getTargetPosition(),
                    inferTarget(userSignal),
                    inferTarget(generatedText)
            );

            memory.setTargetPosition(targetPosition);
            memory.setSkillTags(toJson(skills));
            memory.setGapTags(toJson(gaps));
            memory.setPreferenceTags(toJson(preferences));
            memory.setProfileSummary(clip(buildProfileSummary(targetPosition, skills, gaps), SUMMARY_LIMIT));
            memory.setSuggestionSummary(clip(generatedText, SUMMARY_LIMIT));
            memory.setEvidenceSummary(clip(buildEvidenceSummary(toolCalls), SUMMARY_LIMIT));
            memory.setSource(source);
            memory.setLastInteractionAt(LocalDateTime.now());
            memory.setUpdatedAt(LocalDateTime.now());
            memoryRepository.save(memory);
        } catch (DataAccessException ignored) {
            // The memory table is an optional enhancement. Missing table must not break OfferAgent.
        }
    }

    private OfferAgentMemoryResponse toResponse(OfferAgentMemory memory) {
        return new OfferAgentMemoryResponse(
                memory.getUserId(),
                memory.getTargetPosition(),
                memory.getProfileSummary(),
                parseList(memory.getSkillTags()),
                parseList(memory.getGapTags()),
                parseList(memory.getPreferenceTags()),
                memory.getSuggestionSummary(),
                memory.getEvidenceSummary(),
                memory.getSource(),
                memory.getLastInteractionAt()
        );
    }

    private OfferAgentMemoryResponse empty(Integer userId) {
        OfferAgentMemoryResponse response = new OfferAgentMemoryResponse();
        response.setUserId(userId);
        return response;
    }

    private List<String> extractSkills(String text) {
        String source = safe(text).toLowerCase();
        return KNOWN_SKILLS.stream()
                .filter(skill -> source.contains(skill.toLowerCase()))
                .toList();
    }

    private List<String> extractGaps(String text) {
        List<String> gaps = new ArrayList<>();
        String source = safe(text);
        for (String skill : KNOWN_SKILLS) {
            if ((source.contains("补充" + skill) || source.contains("缺少" + skill) || source.contains("学习" + skill))
                    && !gaps.contains(skill)) {
                gaps.add(skill);
            }
        }
        return gaps;
    }

    private List<String> extractTargetCandidates(String text) {
        String target = inferTarget(text);
        return target.isBlank() ? List.of() : List.of(target);
    }

    private List<String> extractToolArray(List<OfferAgentToolResult> toolCalls, String fieldName) {
        List<String> values = new ArrayList<>();
        for (OfferAgentToolResult result : toolCalls) {
            try {
                JsonNode node = objectMapper.readTree(result.getOutputJson()).path(fieldName);
                if (node.isArray()) {
                    node.forEach(item -> {
                        String value = item.asText("");
                        if (!value.isBlank()) values.add(value);
                    });
                }
            } catch (Exception ignored) {
                // Skip malformed tool output.
            }
        }
        return values;
    }

    @SafeVarargs
    private List<String> merge(List<String>... lists) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        for (List<String> list : lists) {
            if (list == null) continue;
            for (String item : list) {
                if (item != null && !item.isBlank()) {
                    merged.add(item.trim());
                }
            }
        }
        return new ArrayList<>(merged).stream().limit(12).toList();
    }

    private List<String> parseList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> values = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return values == null ? new ArrayList<>() : values;
        } catch (Exception ignored) {
            return List.of(json);
        }
    }

    private String buildProfileSummary(String targetPosition, List<String> skills, List<String> gaps) {
        return "长期画像：目标岗位=" + defaultText(targetPosition)
                + "；已识别技能=" + defaultText(String.join("、", skills))
                + "；待补能力=" + defaultText(String.join("、", gaps));
    }

    private String buildEvidenceSummary(List<OfferAgentToolResult> toolCalls) {
        long success = toolCalls.stream().filter(call -> Boolean.TRUE.equals(call.getSuccess())).count();
        List<String> tools = toolCalls.stream().map(OfferAgentToolResult::getToolName).distinct().toList();
        return "最近记忆依据：成功工具 " + success + "/" + toolCalls.size()
                + "，工具=" + String.join(", ", tools);
    }

    private String inferTarget(String text) {
        String source = safe(text).toLowerCase();
        if (containsAny(source, "前端", "vue", "react")) return "前端开发实习生";
        if (containsAny(source, "后端", "java", "spring")) return "Java 后端实习生";
        if (containsAny(source, "数据", "算法", "python", "sql")) return "数据/算法实习生";
        if (containsAny(source, "产品", "运营", "pm")) return "产品/运营实习生";
        if (containsAny(source, "agent", "rag", "大模型", "llm")) return "AI Agent 实习生";
        return "";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception ignored) {
            return "[]";
        }
    }

    private void append(StringBuilder builder, String key, Object value) {
        builder.append("- ").append(key).append(": ").append(defaultText(value)).append('\n');
    }

    private String first(List<String> values) {
        return values == null || values.isEmpty() ? "" : values.get(0);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String clip(String value, int limit) {
        if (value == null || value.isBlank()) return "";
        return value.length() <= limit ? value : value.substring(0, limit) + "...";
    }

    private String defaultText(Object value) {
        if (value == null) return "unknown";
        String text = String.valueOf(value);
        return text.isBlank() ? "unknown" : text;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
