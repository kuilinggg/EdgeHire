package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
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
        OfferAgentToolPlan plan = buildPlan(conversationId, message, userContext);
        List<OfferAgentToolResult> results = new ArrayList<>();

        for (OfferAgentPlannedToolCall call : plan.getToolCalls()) {
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

        return results;
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
            sanitized.add(new OfferAgentPlannedToolCall(call.getToolName(), arguments));
            if (sanitized.size() >= MAX_TOOL_CALLS_PER_TURN) {
                break;
            }
        }
        return sanitized;
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
