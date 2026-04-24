package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.Entity.OfferAgentToolCallLog;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferAgentToolExecutionService {
    private final OfferAgentToolPlanner planner;
    private final OfferAgentToolRegistry registry;
    private final OfferAgentToolCallLogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<OfferAgentToolResult> execute(Integer userId, String conversationId, String message) {
        OfferAgentToolContext context = new OfferAgentToolContext(userId, conversationId, message);
        List<OfferAgentToolResult> results = new ArrayList<>();

        for (String toolName : planner.plan(message)) {
            OfferAgentToolResult result = registry.find(toolName)
                    .map(tool -> executeTool(tool, context))
                    .orElseGet(() -> missingTool(toolName, context));
            results.add(result);
            saveLog(context, result);
        }

        return results;
    }

    private OfferAgentToolResult executeTool(OfferAgentTool tool, OfferAgentToolContext context) {
        String inputJson = toJson(Map.of(
                "userId", context.getUserId(),
                "conversationId", context.getConversationId() == null ? "" : context.getConversationId(),
                "message", context.getMessage() == null ? "" : context.getMessage()
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
                    null
            );
        } catch (Exception e) {
            return new OfferAgentToolResult(
                    tool.name(),
                    tool.description(),
                    inputJson,
                    "{}",
                    "failed",
                    false,
                    e.getMessage()
            );
        }
    }

    private OfferAgentToolResult missingTool(String toolName, OfferAgentToolContext context) {
        return new OfferAgentToolResult(
                toolName,
                "Missing tool",
                toJson(context),
                "{}",
                "tool not found",
                false,
                "Tool is not registered"
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
