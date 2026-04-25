package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.DTO.OfferAgentWorkflowResponse;
import com.se.EdgeHire.DTO.OfferAgentWorkflowStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferAgentWorkflowService {
    private static final String SPRINT_WORKFLOW_NAME = "AI Agent 实习冲刺规划";
    private static final String RESUME_WORKFLOW_NAME = "简历优化工作流";
    private static final String MOCK_INTERVIEW_WORKFLOW_NAME = "模拟面试工作流";

    private final OfferAgentToolExecutionService toolExecutionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentWorkflowResponse runAiAgentSprint(Integer userId, String conversationId, String targetPosition) {
        String target = targetPosition == null || targetPosition.isBlank()
                ? "AI Agent 实习生"
                : targetPosition.trim();
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-ai-agent-sprint-" + userId
                : conversationId;
        String message = "执行 AI Agent 实习冲刺规划工作流，目标岗位：" + target;

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ProfileAgent", List.of(
                call("get_user_profile"),
                call("get_job_intention")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "ResumeAgent", List.of(
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "KnowledgeAgent", List.of(
                call("retrieve_knowledge", Map.of("query", target + " RAG Tool Calling AI Agent 面试 简历 项目表达", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "MatchAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", target))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "PlannerAgent", List.of(
                call("generate_interview_plan", Map.of("targetPosition", target, "days", 7))
        )));

        return new OfferAgentWorkflowResponse(
                SPRINT_WORKFLOW_NAME,
                target,
                workflowConversationId,
                steps,
                buildSprintFinalReport(target, steps)
        );
    }

    public OfferAgentWorkflowResponse runResumeOptimization(Integer userId, String conversationId, String targetPosition) {
        String target = targetPosition == null || targetPosition.isBlank()
                ? "AI Agent 实习生"
                : targetPosition.trim();
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-resume-optimization-" + userId
                : conversationId;
        String message = "执行简历优化工作流，目标岗位：" + target;

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ResumeParserAgent", List.of(
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "JDAnalysisAgent", List.of(
                call("get_job_intention"),
                call("retrieve_knowledge", Map.of("query", target + " 岗位描述 技能关键词 简历筛选", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "ResumeCoachAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", target))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "RAGAgent", List.of(
                call("retrieve_knowledge", Map.of("query", target + " 简历优化 STAR 法则 量化 项目经历", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "RewriteAgent", List.of(
                call("get_resume_summary"),
                call("retrieve_knowledge", Map.of("query", "简历改写 示例 RAG Tool Calling AI Agent 项目经历", "topK", 3))
        )));

        return new OfferAgentWorkflowResponse(
                RESUME_WORKFLOW_NAME,
                target,
                workflowConversationId,
                steps,
                buildResumeOptimizationReport(target, steps)
        );
    }

    public OfferAgentWorkflowResponse runMockInterview(Integer userId, String conversationId, String targetPosition) {
        String target = targetPosition == null || targetPosition.isBlank()
                ? "AI Agent 实习生"
                : targetPosition.trim();
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-mock-interview-" + userId
                : conversationId;
        String message = "执行模拟面试工作流，目标岗位：" + target;

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ProfileAgent", List.of(
                call("get_user_profile"),
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "InterviewQuestionAgent", List.of(
                call("generate_interview_plan", Map.of("targetPosition", target, "days", 3))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "EvaluatorAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", target))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "KnowledgeAgent", List.of(
                call("retrieve_knowledge", Map.of("query", target + " 模拟面试 RAG Tool Calling 答题框架 追问", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "CoachAgent", List.of(
                call("retrieve_knowledge", Map.of("query", "AI Agent 面试 回答优化 STAR 项目讲解", "topK", 3)),
                call("generate_interview_plan", Map.of("targetPosition", target, "days", 7))
        )));

        return new OfferAgentWorkflowResponse(
                MOCK_INTERVIEW_WORKFLOW_NAME,
                target,
                workflowConversationId,
                steps,
                buildMockInterviewReport(target, steps)
        );
    }

    private OfferAgentWorkflowStep runStep(
            Integer userId,
            String conversationId,
            String message,
            String agentName,
            List<OfferAgentPlannedToolCall> calls) {
        OfferAgentToolExecutionReport report = toolExecutionService.executePlannedCalls(
                userId,
                conversationId,
                message,
                calls,
                "workflow"
        );
        return new OfferAgentWorkflowStep(
                agentName,
                report.getToolCalls().stream().allMatch(OfferAgentToolResult::getSuccess) ? "success" : "partial",
                summarize(agentName, report.getToolCalls()),
                report.getToolCalls()
        );
    }

    private OfferAgentPlannedToolCall call(String toolName) {
        return call(toolName, Map.of());
    }

    private OfferAgentPlannedToolCall call(String toolName, Map<String, Object> arguments) {
        return new OfferAgentPlannedToolCall(toolName, new LinkedHashMap<>(arguments));
    }

    private String summarize(String agentName, List<OfferAgentToolResult> results) {
        long successCount = results.stream().filter(OfferAgentToolResult::getSuccess).count();
        String suffix = "（成功调用 " + successCount + "/" + results.size() + " 个工具）";
        return switch (agentName) {
            case "ProfileAgent" -> "已读取用户画像、求职意向和简历基础信息" + suffix;
            case "ResumeAgent" -> "已分析简历内容并提取可用于求职表达的技能关键词" + suffix;
            case "KnowledgeAgent" -> "已检索目标岗位、RAG、Tool Calling 和面试相关知识" + suffix;
            case "MatchAgent" -> "已计算目标岗位匹配度，并识别能力差距" + suffix;
            case "PlannerAgent" -> "已生成阶段性求职冲刺计划和面试准备重点" + suffix;
            case "ResumeParserAgent" -> "已读取简历并提取核心技能、项目经历和简历概览" + suffix;
            case "JDAnalysisAgent" -> "已结合目标岗位和知识库分析岗位关键词与筛选关注点" + suffix;
            case "ResumeCoachAgent" -> "已诊断简历与目标岗位之间的匹配度和证据缺口" + suffix;
            case "RAGAgent" -> "已检索简历优化方法、项目表达和 STAR 改写知识" + suffix;
            case "RewriteAgent" -> "已整合简历内容与知识库，生成项目经历改写方向" + suffix;
            case "InterviewQuestionAgent" -> "已生成目标岗位相关面试题和准备主题" + suffix;
            case "EvaluatorAgent" -> "已评估面试准备度，并识别可能被追问的风险点" + suffix;
            case "CoachAgent" -> "已生成答题改进建议、追问训练方向和复盘重点" + suffix;
            default -> "该 Agent 已完成当前步骤" + suffix;
        };
    }

    private String buildSprintFinalReport(String target, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = findStringArray(steps, "calculate_job_match_score", "missingKeywords");
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## AI Agent 实习冲刺规划报告\n\n");
        report.append("- 目标岗位：").append(target).append("\n");
        report.append("- 协作 Agent：ProfileAgent、ResumeAgent、KnowledgeAgent、MatchAgent、PlannerAgent\n");
        if (score != null) {
            report.append("- 岗位匹配度：").append(score).append("/100\n");
        }
        if (!skills.isEmpty()) {
            report.append("- 简历中已覆盖的关键词：").append(String.join("、", skills)).append("\n");
        }
        if (!missing.isEmpty()) {
            report.append("- 优先补强方向：").append(String.join("、", missing.subList(0, Math.min(5, missing.size())))).append("\n");
        }
        if (hitCount != null) {
            report.append("- RAG 检索命中数量：").append(hitCount).append("\n");
        }
        report.append("\n### 7 天冲刺重点\n");
        report.append("1. 梳理 OfferAgent 项目主线：RAG 检索、Tool Calling、VectorStore 和多 Agent 工作流分别解决什么问题。\n");
        report.append("2. 把简历项目描述改成可量化表达，例如检索模式、工具数量、fallback 机制、日志追踪和测试覆盖。\n");
        report.append("3. 准备面试回答：为什么要拆分 Agent、为什么工具必须由后端校验执行、异常时如何降级。\n");
        report.append("4. 完整演示一次从用户画像到最终行动计划的流程，并能解释每个后端服务和接口。\n");
        return report.toString();
    }

    private String buildResumeOptimizationReport(String target, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = findStringArray(steps, "calculate_job_match_score", "missingKeywords");
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## 简历优化报告\n\n");
        report.append("- 目标岗位：").append(target).append("\n");
        report.append("- 协作 Agent：ResumeParserAgent、JDAnalysisAgent、ResumeCoachAgent、RAGAgent、RewriteAgent\n");
        if (score != null) {
            report.append("- 当前简历匹配度：").append(score).append("/100\n");
        }
        if (!skills.isEmpty()) {
            report.append("- 已覆盖技术关键词：").append(String.join("、", skills)).append("\n");
        }
        if (!missing.isEmpty()) {
            report.append("- 需要补强的关键词或证据：")
                    .append(String.join("、", missing.subList(0, Math.min(5, missing.size()))))
                    .append("\n");
        }
        if (hitCount != null) {
            report.append("- 简历优化知识命中数量：").append(hitCount).append("\n");
        }
        report.append("\n### 优化优先级\n");
        report.append("1. 在简历靠前位置明确目标岗位、AI Agent 关键词和项目业务结果。\n");
        report.append("2. 项目经历按“动作 + 技术设计 + 结果”改写，重点突出 RAG、Tool Calling、VectorStore 和工作流追踪。\n");
        report.append("3. 增加工程化证据，例如测试覆盖、检索命中、降级策略、工具/Agent 数量、日志追踪等。\n");
        report.append("4. 把笼统描述改成具体实现：数据表、接口、服务类、参数校验和安全边界。\n");
        report.append("\n### 改写示例方向\n");
        report.append("- 改写前：实现了一个 AI 求职助手。\n");
        report.append("- 改写后：设计并实现 OfferAgent 智能求职助手，集成 RAG 检索、LLM 工具规划、后端工具白名单校验和多 Agent 工作流编排，支持简历诊断、岗位匹配和模拟面试等完整求职场景。\n");
        return report.toString();
    }

    private String buildMockInterviewReport(String target, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = findStringArray(steps, "calculate_job_match_score", "missingKeywords");
        List<String> questions = findStringArray(steps, "generate_interview_plan", "questions");
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## 模拟面试报告\n\n");
        report.append("- 目标岗位：").append(target).append("\n");
        report.append("- 协作 Agent：ProfileAgent、InterviewQuestionAgent、EvaluatorAgent、KnowledgeAgent、CoachAgent\n");
        if (score != null) {
            report.append("- 面试准备度：").append(score).append("/100\n");
        }
        if (!skills.isEmpty()) {
            report.append("- 简历中可支撑讲解的话题：").append(String.join("、", skills)).append("\n");
        }
        if (!missing.isEmpty()) {
            report.append("- 可能被追问的风险点：")
                    .append(String.join("、", missing.subList(0, Math.min(5, missing.size()))))
                    .append("\n");
        }
        if (hitCount != null) {
            report.append("- 面试知识命中数量：").append(hitCount).append("\n");
        }
        report.append("\n### 面试题清单\n");
        if (questions.isEmpty()) {
            report.append("1. 请介绍 OfferAgent 项目的整体架构，以及为什么需要 RAG。\n");
            report.append("2. 你的 Tool Calling 层如何避免不可信模型输出直接影响业务数据？\n");
            report.append("3. 多 Agent 工作流中每个 Agent 分别负责什么，为什么要这样拆分？\n");
        } else {
            for (int i = 0; i < Math.min(5, questions.size()); i++) {
                report.append(i + 1).append(". ").append(questions.get(i)).append("\n");
            }
        }
        report.append("\n### 答题建议\n");
        report.append("1. 先讲业务目标，再讲架构、数据流和安全控制，不要一上来堆技术名词。\n");
        report.append("2. 每个项目能力都准备一个具体接口、服务类、数据表和测试用例。\n");
        report.append("3. 被问到 Agent 设计时，要说明 LLM 负责规划，确定性工具负责执行，后端负责校验和 fallback。\n");
        report.append("4. 准备一个失败案例，例如检索无命中、非法工具调用、简历信息缺失，并说明系统如何降级。\n");
        report.append("\n### 追问训练\n");
        report.append("- 如果去掉大模型，系统哪些部分仍然可用？为什么？\n");
        report.append("- 你会如何评估 RAG 检索质量和工具调用正确性？\n");
        return report.toString();
    }

    private List<String> findStringArray(List<OfferAgentWorkflowStep> steps, String toolName, String fieldName) {
        for (OfferAgentToolResult result : allResults(steps)) {
            if (!toolName.equals(result.getToolName())) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(result.getOutputJson()).path(fieldName);
                if (node.isArray()) {
                    List<String> values = new ArrayList<>();
                    node.forEach(item -> values.add(item.asText()));
                    return values;
                }
            } catch (Exception ignored) {
                return List.of();
            }
        }
        return List.of();
    }

    private Integer findInt(List<OfferAgentWorkflowStep> steps, String toolName, String fieldName) {
        for (OfferAgentToolResult result : allResults(steps)) {
            if (!toolName.equals(result.getToolName())) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(result.getOutputJson()).path(fieldName);
                return node.isNumber() ? node.asInt() : null;
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private List<OfferAgentToolResult> allResults(List<OfferAgentWorkflowStep> steps) {
        return steps.stream()
                .flatMap(step -> step.getToolCalls().stream())
                .toList();
    }
}
