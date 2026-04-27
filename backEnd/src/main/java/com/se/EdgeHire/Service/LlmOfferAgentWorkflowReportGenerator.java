package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentAiRequest;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.DTO.OfferAgentWorkflowStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmOfferAgentWorkflowReportGenerator implements OfferAgentWorkflowReportGenerator {
    private static final Duration REPORT_TIMEOUT = Duration.ofSeconds(60);
    private static final int MAX_TOOL_OUTPUT_CHARS = 1600;

    private final WebClient webClient;

    @Override
    public String generate(
            String conversationId,
            String workflowName,
            String targetPosition,
            List<OfferAgentWorkflowStep> steps,
            String fallbackReport) {
        List<String> chunks = generateStream(conversationId, workflowName, targetPosition, steps, fallbackReport)
                .collectList()
                .block(REPORT_TIMEOUT);
        String report = chunks == null ? "" : String.join("", chunks).trim();
        if (report.isBlank() || report.startsWith("Error:")) {
            return fallback(fallbackReport, "AI 模块返回为空或出错，已使用确定性 fallback 报告。");
        }
        return report;
    }

    @Override
    public Flux<String> generateStream(
            String conversationId,
            String workflowName,
            String targetPosition,
            List<OfferAgentWorkflowStep> steps,
            String fallbackReport) {
        try {
            OfferAgentAiRequest request = new OfferAgentAiRequest(
                    conversationId + "-final-report-" + System.currentTimeMillis(),
                    buildMessage(workflowName, targetPosition),
                    buildToolContext(workflowName, targetPosition, steps, fallbackReport)
            );
            return webClient.post()
                    .uri("/api/offer-agent/chat/stream")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .timeout(REPORT_TIMEOUT)
                    .onErrorResume(e -> Flux.just(fallback(fallbackReport, "AI 模块暂时不可用，已使用确定性 fallback 报告。")));
        } catch (Exception e) {
            return Flux.just(fallback(fallbackReport, "AI 模块暂时不可用，已使用确定性 fallback 报告。"));
        }
    }

    private String buildMessage(String workflowName, String targetPosition) {
        return """
                请基于 OfferAgent 多 Agent 工作流的真实工具执行结果，生成一份中文最终报告。

                工作流：%s
                用户当前填写的目标岗位：%s

                写作要求：
                - 报告必须围绕当前目标岗位，不要套用固定岗位模板。
                - 结合工具结果中的简历技能、岗位匹配、RAG 检索命中和面试计划。
                - 如果目标岗位变化，关键词、能力差距、行动建议和面试准备也要随之变化。
                - 使用清晰的 Markdown 段落和列表，标题和正文都使用中文。
                - Markdown 标题必须独占一行，# 后面必须有空格，标题前后保留空行。
                - 可以参考“结论、关键证据、能力差距、行动计划、面试表达”等结构，但不必机械包含所有栏目。
                - 不要解释自己是大模型，不要输出英文标题。
                """.formatted(workflowName, targetPosition);
    }

    private String buildToolContext(
            String workflowName,
            String targetPosition,
            List<OfferAgentWorkflowStep> steps,
            String fallbackReport) {
        StringBuilder builder = new StringBuilder();
        builder.append("Workflow Name: ").append(workflowName).append("\n");
        builder.append("Target Position: ").append(targetPosition).append("\n\n");
        builder.append("Tool Execution Trace:\n");
        for (OfferAgentWorkflowStep step : steps) {
            builder.append("\n[Agent] ").append(step.getAgentName()).append("\n");
            builder.append("Status: ").append(step.getStatus()).append("\n");
            builder.append("Step Summary: ").append(step.getSummary()).append("\n");
            for (OfferAgentToolResult result : step.getToolCalls()) {
                builder.append("- Tool: ").append(result.getToolName()).append("\n");
                builder.append("  Success: ").append(result.getSuccess()).append("\n");
                builder.append("  Input: ").append(trim(result.getInputJson())).append("\n");
                builder.append("  Output: ").append(trim(result.getOutputJson())).append("\n");
            }
        }
        builder.append("\nDeterministic fallback report for reference only. Rewrite it with the current target position and tool evidence:\n");
        builder.append(fallbackReport);
        return builder.toString();
    }

    private String trim(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= MAX_TOOL_OUTPUT_CHARS) {
            return value;
        }
        return value.substring(0, MAX_TOOL_OUTPUT_CHARS) + "...";
    }

    private String fallback(String fallbackReport, String reason) {
        return fallbackReport + "\n\n> 说明：" + reason;
    }
}
