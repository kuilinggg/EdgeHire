package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolPlan;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.Entity.OfferAgentToolCallLog;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OfferAgentToolExecutionService {
    private static final int MAX_TOOL_CALLS_PER_TURN = 6;

    private final OfferAgentToolPlanner rulePlanner;
    private final LlmOfferAgentToolPlanner llmPlanner;
    private final OfferAgentToolRegistry registry;
    private final OfferAgentToolCallLogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<OfferAgentToolResult> execute(Integer userId, String conversationId, String message) {
        return execute(userId, conversationId, message, "");
    }

    public List<OfferAgentToolResult> execute(Integer userId, String conversationId, String message, String userContext) {
        return executeWithReport(userId, conversationId, message, userContext).getToolCalls();
    }

    public OfferAgentToolExecutionReport executeWithReport(
            Integer userId,
            String conversationId,
            String message,
            String userContext) {
        OfferAgentToolPlan plan = buildPlan(conversationId, message, userContext);
        return executePlannedCalls(userId, conversationId, message, plan);
    }

    public OfferAgentToolExecutionReport executePlannedCalls(
            Integer userId,
            String conversationId,
            String message,
            List<OfferAgentPlannedToolCall> calls,
            String planSource) {
        OfferAgentToolPlan plan = new OfferAgentToolPlan(planSource, null, sanitize(calls));
        return executePlannedCalls(userId, conversationId, message, plan);
    }

    private OfferAgentToolExecutionReport executePlannedCalls(
            Integer userId,
            String conversationId,
            String message,
            OfferAgentToolPlan plan) {
        List<OfferAgentToolResult> results = new ArrayList<>();
        List<String> trace = new ArrayList<>();

        trace.add("received_user_question");
        trace.add("tool_plan_source=" + plan.getSource());
        if (plan.getFallbackReason() != null && !plan.getFallbackReason().isBlank()) {
            trace.add("fallback_reason=" + plan.getFallbackReason());
        }
        trace.add("validated_tool_calls=" + plan.getToolCalls().size());

        for (OfferAgentPlannedToolCall call : plan.getToolCalls()) {
            trace.add("execute_tool=" + call.getToolName());
            OfferAgentToolContext context = new OfferAgentToolContext(
                    userId,
                    conversationId,
                    message,
                    call.getArguments()
            );
            OfferAgentToolResult result = registry.find(call.getToolName())
                    .map(tool -> executeTool(tool, context, plan.getSource()))
                    .orElseGet(() -> missingTool(call.getToolName(), context, plan.getSource()));
            results.add(result);
            saveLog(context, result);
        }

        trace.add("tool_execution_finished");
        return new OfferAgentToolExecutionReport(plan.getSource(), plan.getFallbackReason(), trace, results);
    }

    private OfferAgentToolPlan buildPlan(String conversationId, String message, String userContext) {
        return llmPlanner.plan(conversationId, message, userContext)
                .map(plan -> {
                    List<OfferAgentPlannedToolCall> calls = sanitize(plan.getToolCalls());
                    if (calls.isEmpty()) {
                        return fallbackPlan(message, "llm planner returned no valid tool call");
                    }
                    return new OfferAgentToolPlan("llm", null, calls);
                })
                .orElseGet(() -> fallbackPlan(message, "llm planner unavailable"));
    }

    private OfferAgentToolPlan fallbackPlan(String message, String reason) {
        return new OfferAgentToolPlan("rule_fallback", reason, sanitize(rulePlanner.planCalls(message)));
    }

    private List<OfferAgentPlannedToolCall> sanitize(List<OfferAgentPlannedToolCall> calls) {
        if (calls == null || calls.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<OfferAgentPlannedToolCall> sanitized = new ArrayList<>();
        for (OfferAgentPlannedToolCall call : calls) {
            if (call == null || call.getToolName() == null || !registry.contains(call.getToolName())) {
                continue;
            }
            if (!seen.add(call.getToolName())) {
                continue;
            }
            Map<String, Object> arguments = call.getArguments() == null
                    ? new LinkedHashMap<>()
                    : new LinkedHashMap<>(call.getArguments());
            arguments = sanitizeArguments(call.getToolName(), arguments);
            sanitized.add(new OfferAgentPlannedToolCall(call.getToolName(), arguments));
            if (sanitized.size() >= MAX_TOOL_CALLS_PER_TURN) {
                break;
            }
        }
        return sanitized;
    }

    private Map<String, Object> sanitizeArguments(String toolName, Map<String, Object> arguments) {
        LinkedHashMap<String, Object> sanitized = new LinkedHashMap<>();
        if ("retrieve_knowledge".equals(toolName)) {
            String query = readString(arguments.get("query"), 200);
            if (!query.isBlank()) {
                sanitized.put("query", query);
            }
            sanitized.put("topK", readInt(arguments.get("topK"), 5, 1, 8));
            return sanitized;
        }
        if ("calculate_job_match_score".equals(toolName)) {
            String targetPosition = readString(arguments.get("targetPosition"), 100);
            if (!targetPosition.isBlank()) {
                sanitized.put("targetPosition", targetPosition);
            }
            return sanitized;
        }
        if ("generate_interview_plan".equals(toolName)) {
            String targetPosition = readString(arguments.get("targetPosition"), 100);
            if (!targetPosition.isBlank()) {
                sanitized.put("targetPosition", targetPosition);
            }
            sanitized.put("days", readInt(arguments.get("days"), 7, 1, 14));
            return sanitized;
        }
        return sanitized;
    }

    private String readString(Object value, int maxLength) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private int readInt(Object value, int defaultValue, int min, int max) {
        try {
            int parsed = Integer.parseInt(String.valueOf(value));
            return Math.max(min, Math.min(max, parsed));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private OfferAgentToolResult executeTool(OfferAgentTool tool, OfferAgentToolContext context, String planSource) {
        String inputJson = toJson(Map.of(
                "userId", context.getUserId(),
                "conversationId", context.getConversationId() == null ? "" : context.getConversationId(),
                "message", context.getMessage() == null ? "" : context.getMessage(),
                "arguments", context.getArguments()
        ));
        try {
            Map<String, Object> output = tool.execute(context);
            String outputJson = toJson(output);
            return new OfferAgentToolResult(
                    tool.name(),
                    tool.description(),
                    inputJson,
                    outputJson,
                    String.valueOf(output.getOrDefault("summary", "executed")),
                    true,
                    null,
                    planSource
            );
        } catch (Exception e) {
            return new OfferAgentToolResult(
                    tool.name(),
                    tool.description(),
                    inputJson,
                    "{}",
                    "failed",
                    false,
                    e.getMessage(),
                    planSource
            );
        }
    }

    private OfferAgentToolResult missingTool(String toolName, OfferAgentToolContext context, String planSource) {
        return new OfferAgentToolResult(
                toolName,
                "Missing tool",
                toJson(context),
                "{}",
                "tool not found",
                false,
                "Tool is not registered",
                planSource
        );
    }

    private void saveLog(OfferAgentToolContext context, OfferAgentToolResult result) {
        try {
            OfferAgentToolCallLog log = new OfferAgentToolCallLog();
            log.setUserId(context.getUserId());
            log.setConversationId(context.getConversationId());
            log.setMessage(context.getMessage());
            log.setToolName(result.getToolName());
            log.setInputJson(result.getInputJson());
            log.setOutputJson(result.getOutputJson());
            log.setSuccess(result.getSuccess());
            log.setErrorMessage(result.getErrorMessage());
            log.setPlanSource(result.getPlanSource());
            logRepository.save(log);
        } catch (Exception ignored) {
            // Tool logging must not break the agent response.
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
