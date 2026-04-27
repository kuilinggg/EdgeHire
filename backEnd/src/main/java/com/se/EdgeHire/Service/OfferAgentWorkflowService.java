package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.DTO.OfferAgentWorkflowResponse;
import com.se.EdgeHire.DTO.OfferAgentWorkflowStreamEvent;
import com.se.EdgeHire.DTO.OfferAgentWorkflowStep;
import com.se.EdgeHire.Repository.SeekerInfoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OfferAgentWorkflowService {
    private static final String SPRINT_WORKFLOW_NAME = "AI Agent 实习冲刺规划";
    private static final String RESUME_WORKFLOW_NAME = "简历优化工作流";
    private static final String MOCK_INTERVIEW_WORKFLOW_NAME = "模拟面试工作流";

    private final OfferAgentToolExecutionService toolExecutionService;
    private final OfferAgentWorkflowReportGenerator reportGenerator;
    private final SeekerInfoRepository seekerInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentWorkflowResponse runAiAgentSprint(Integer userId, String conversationId, String targetPosition) {
        RoleProfile profile = roleProfile(resolveTargetPosition(userId, targetPosition));
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-ai-agent-sprint-" + userId
                : conversationId;
        String message = "执行求职冲刺规划工作流，目标岗位：" + profile.target();

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ProfileAgent", List.of(
                call("get_user_profile"),
                call("get_job_intention")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "ResumeAgent", List.of(
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "KnowledgeAgent", List.of(
                call("retrieve_knowledge", Map.of("query", profile.target() + " " + String.join(" ", profile.coreKeywords()) + " 面试 简历 项目表达", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "MatchAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", profile.target()))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "PlannerAgent", List.of(
                call("generate_interview_plan", Map.of("targetPosition", profile.target(), "days", 7))
        )));

        String fallbackReport = buildSprintFinalReport(profile, steps);
        return new OfferAgentWorkflowResponse(
                SPRINT_WORKFLOW_NAME,
                profile.target(),
                workflowConversationId,
                steps,
                reportGenerator.generate(workflowConversationId, SPRINT_WORKFLOW_NAME, profile.target(), steps, fallbackReport)
        );
    }

    public OfferAgentWorkflowResponse runResumeOptimization(Integer userId, String conversationId, String targetPosition) {
        RoleProfile profile = roleProfile(resolveTargetPosition(userId, targetPosition));
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-resume-optimization-" + userId
                : conversationId;
        String message = "执行简历优化工作流，目标岗位：" + profile.target();

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ResumeParserAgent", List.of(
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "JDAnalysisAgent", List.of(
                call("get_job_intention"),
                call("retrieve_knowledge", Map.of("query", profile.target() + " 岗位描述 技能关键词 简历筛选 " + String.join(" ", profile.coreKeywords()), "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "ResumeCoachAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", profile.target()))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "RAGAgent", List.of(
                call("retrieve_knowledge", Map.of("query", profile.target() + " 简历优化 STAR 法则 量化 项目经历", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "RewriteAgent", List.of(
                call("get_resume_summary"),
                call("retrieve_knowledge", Map.of("query", profile.target() + " 简历改写 示例 项目经历 " + String.join(" ", profile.coreKeywords()), "topK", 3))
        )));

        String fallbackReport = buildResumeOptimizationReport(profile, steps);
        return new OfferAgentWorkflowResponse(
                RESUME_WORKFLOW_NAME,
                profile.target(),
                workflowConversationId,
                steps,
                reportGenerator.generate(workflowConversationId, RESUME_WORKFLOW_NAME, profile.target(), steps, fallbackReport)
        );
    }

    public OfferAgentWorkflowResponse runMockInterview(Integer userId, String conversationId, String targetPosition) {
        RoleProfile profile = roleProfile(resolveTargetPosition(userId, targetPosition));
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-mock-interview-" + userId
                : conversationId;
        String message = "执行模拟面试工作流，目标岗位：" + profile.target();

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ProfileAgent", List.of(
                call("get_user_profile"),
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "InterviewQuestionAgent", List.of(
                call("generate_interview_plan", Map.of("targetPosition", profile.target(), "days", 3))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "EvaluatorAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", profile.target()))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "KnowledgeAgent", List.of(
                call("retrieve_knowledge", Map.of("query", profile.target() + " 模拟面试 答题框架 追问 " + String.join(" ", profile.coreKeywords()), "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "CoachAgent", List.of(
                call("retrieve_knowledge", Map.of("query", profile.target() + " 面试回答优化 STAR 项目讲解", "topK", 3)),
                call("generate_interview_plan", Map.of("targetPosition", profile.target(), "days", 7))
        )));

        String fallbackReport = buildMockInterviewReport(profile, steps);
        return new OfferAgentWorkflowResponse(
                MOCK_INTERVIEW_WORKFLOW_NAME,
                profile.target(),
                workflowConversationId,
                steps,
                reportGenerator.generate(workflowConversationId, MOCK_INTERVIEW_WORKFLOW_NAME, profile.target(), steps, fallbackReport)
        );
    }

    public Flux<OfferAgentWorkflowStreamEvent> streamAiAgentSprint(Integer userId, String conversationId, String targetPosition) {
        RoleProfile profile = roleProfile(resolveTargetPosition(userId, targetPosition));
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-ai-agent-sprint-" + userId
                : conversationId;
        String message = "执行求职冲刺规划工作流，目标岗位：" + profile.target();
        return streamWorkflow(
                userId,
                workflowConversationId,
                message,
                SPRINT_WORKFLOW_NAME,
                profile,
                List.of(
                        new WorkflowStepPlan("ProfileAgent", List.of(call("get_user_profile"), call("get_job_intention"))),
                        new WorkflowStepPlan("ResumeAgent", List.of(call("get_resume_summary"))),
                        new WorkflowStepPlan("KnowledgeAgent", List.of(call("retrieve_knowledge", Map.of("query", profile.target() + " " + String.join(" ", profile.coreKeywords()) + " 面试 简历 项目表达", "topK", 5)))),
                        new WorkflowStepPlan("MatchAgent", List.of(call("calculate_job_match_score", Map.of("targetPosition", profile.target())))),
                        new WorkflowStepPlan("PlannerAgent", List.of(call("generate_interview_plan", Map.of("targetPosition", profile.target(), "days", 7))))
                ),
                steps -> buildSprintFinalReport(profile, steps)
        );
    }

    public Flux<OfferAgentWorkflowStreamEvent> streamResumeOptimization(Integer userId, String conversationId, String targetPosition) {
        RoleProfile profile = roleProfile(resolveTargetPosition(userId, targetPosition));
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-resume-optimization-" + userId
                : conversationId;
        String message = "执行简历优化工作流，目标岗位：" + profile.target();
        return streamWorkflow(
                userId,
                workflowConversationId,
                message,
                RESUME_WORKFLOW_NAME,
                profile,
                List.of(
                        new WorkflowStepPlan("ResumeParserAgent", List.of(call("get_resume_summary"))),
                        new WorkflowStepPlan("JDAnalysisAgent", List.of(
                                call("get_job_intention"),
                                call("retrieve_knowledge", Map.of("query", profile.target() + " 岗位描述 技能关键词 简历筛选 " + String.join(" ", profile.coreKeywords()), "topK", 5))
                        )),
                        new WorkflowStepPlan("ResumeCoachAgent", List.of(call("calculate_job_match_score", Map.of("targetPosition", profile.target())))),
                        new WorkflowStepPlan("RAGAgent", List.of(call("retrieve_knowledge", Map.of("query", profile.target() + " 简历优化 STAR 法则 量化 项目经历", "topK", 5)))),
                        new WorkflowStepPlan("RewriteAgent", List.of(
                                call("get_resume_summary"),
                                call("retrieve_knowledge", Map.of("query", profile.target() + " 简历改写 示例 项目经历 " + String.join(" ", profile.coreKeywords()), "topK", 3))
                        ))
                ),
                steps -> buildResumeOptimizationReport(profile, steps)
        );
    }

    public Flux<OfferAgentWorkflowStreamEvent> streamMockInterview(Integer userId, String conversationId, String targetPosition) {
        RoleProfile profile = roleProfile(resolveTargetPosition(userId, targetPosition));
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-mock-interview-" + userId
                : conversationId;
        String message = "执行模拟面试工作流，目标岗位：" + profile.target();
        return streamWorkflow(
                userId,
                workflowConversationId,
                message,
                MOCK_INTERVIEW_WORKFLOW_NAME,
                profile,
                List.of(
                        new WorkflowStepPlan("ProfileAgent", List.of(call("get_user_profile"), call("get_resume_summary"))),
                        new WorkflowStepPlan("InterviewQuestionAgent", List.of(call("generate_interview_plan", Map.of("targetPosition", profile.target(), "days", 3)))),
                        new WorkflowStepPlan("EvaluatorAgent", List.of(call("calculate_job_match_score", Map.of("targetPosition", profile.target())))),
                        new WorkflowStepPlan("KnowledgeAgent", List.of(call("retrieve_knowledge", Map.of("query", profile.target() + " 模拟面试 答题框架 追问 " + String.join(" ", profile.coreKeywords()), "topK", 5)))),
                        new WorkflowStepPlan("CoachAgent", List.of(
                                call("retrieve_knowledge", Map.of("query", profile.target() + " 面试回答优化 STAR 项目讲解", "topK", 3)),
                                call("generate_interview_plan", Map.of("targetPosition", profile.target(), "days", 7))
                        ))
                ),
                steps -> buildMockInterviewReport(profile, steps)
        );
    }

    private Flux<OfferAgentWorkflowStreamEvent> streamWorkflow(
            Integer userId,
            String conversationId,
            String message,
            String workflowName,
            RoleProfile profile,
            List<WorkflowStepPlan> plans,
            Function<List<OfferAgentWorkflowStep>, String> fallbackBuilder) {
        return Flux.<OfferAgentWorkflowStreamEvent>create(sink -> Schedulers.boundedElastic().schedule(() -> {
            List<OfferAgentWorkflowStep> steps = new ArrayList<>();
            try {
                sink.next(event("workflow_start", workflowName, profile.target(), conversationId, null, null, null, "工作流已启动", false));
                for (WorkflowStepPlan plan : plans) {
                    sink.next(event("step_start", workflowName, profile.target(), conversationId, plan.agentName(), null, null, plan.agentName() + " 正在执行", false));
                    OfferAgentWorkflowStep step = runStep(userId, conversationId, message, plan.agentName(), plan.calls());
                    steps.add(step);
                    sink.next(event("step_complete", workflowName, profile.target(), conversationId, plan.agentName(), step, null, step.getSummary(), false));
                }
                sink.next(event("report_start", workflowName, profile.target(), conversationId, null, null, null, "开始生成最终报告", false));
                String fallbackReport = fallbackBuilder.apply(steps);
                StringBuilder finalReport = new StringBuilder();
                reportGenerator.generateStream(conversationId, workflowName, profile.target(), steps, fallbackReport)
                        .doOnNext(chunk -> {
                            if (chunk != null && !chunk.isBlank()) {
                                finalReport.append(chunk);
                                sink.next(event("report_delta", workflowName, profile.target(), conversationId, null, null, chunk, "最终报告生成中", false));
                            }
                        })
                        .doOnError(error -> {
                            sink.next(event("error", workflowName, profile.target(), conversationId, null, null, null, "工作流执行失败：" + error.getMessage(), true));
                            sink.complete();
                        })
                        .doOnComplete(() -> {
                            sink.next(event("workflow_done", workflowName, profile.target(), conversationId, null, null, finalReport.toString(), "工作流执行完成", true));
                            sink.complete();
                        })
                        .subscribe();
            } catch (Exception e) {
                sink.next(event("error", workflowName, profile.target(), conversationId, null, null, null, "工作流执行失败：" + e.getMessage(), true));
                sink.complete();
            }
        }));
    }

    private OfferAgentWorkflowStreamEvent event(
            String type,
            String workflowName,
            String targetPosition,
            String conversationId,
            String agentName,
            OfferAgentWorkflowStep step,
            String content,
            String message,
            Boolean done) {
        return new OfferAgentWorkflowStreamEvent(type, workflowName, targetPosition, conversationId, agentName, step, content, message, done);
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
            case "KnowledgeAgent" -> "已检索目标岗位、技能要求和面试相关知识" + suffix;
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

    private String buildSprintFinalReport(RoleProfile profile, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = mergeGaps(profile, skills, findStringArray(steps, "calculate_job_match_score", "missingKeywords"));
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## ").append(profile.target()).append("冲刺规划报告\n\n");
        report.append("- 目标岗位：").append(profile.target()).append("\n");
        report.append("- 岗位方向：").append(profile.category()).append("\n");
        report.append("- 核心关键词：").append(String.join("、", profile.coreKeywords())).append("\n");
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
        appendNumbered(report, profile.sprintFocus());
        report.append("\n### 本岗位演示重点\n");
        report.append("- 面试中要把项目讲成“").append(profile.demoAngle()).append("”，而不是只罗列技术名词。\n");
        return report.toString();
    }

    private String buildResumeOptimizationReport(RoleProfile profile, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = mergeGaps(profile, skills, findStringArray(steps, "calculate_job_match_score", "missingKeywords"));
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## ").append(profile.target()).append("简历优化报告\n\n");
        report.append("- 目标岗位：").append(profile.target()).append("\n");
        report.append("- 岗位方向：").append(profile.category()).append("\n");
        report.append("- 岗位关键词：").append(String.join("、", profile.coreKeywords())).append("\n");
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
        appendNumbered(report, profile.resumePriorities());
        report.append("\n### 改写示例方向\n");
        report.append("- 改写前：参与开发了一个求职平台功能。\n");
        report.append("- 改写后：").append(profile.rewriteExample()).append("\n");
        return report.toString();
    }

    private String buildMockInterviewReport(RoleProfile profile, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = mergeGaps(profile, skills, findStringArray(steps, "calculate_job_match_score", "missingKeywords"));
        List<String> questions = profile.interviewQuestions();
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## ").append(profile.target()).append("模拟面试报告\n\n");
        report.append("- 目标岗位：").append(profile.target()).append("\n");
        report.append("- 岗位方向：").append(profile.category()).append("\n");
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
        appendNumbered(report, questions.subList(0, Math.min(5, questions.size())));
        report.append("\n### 答题建议\n");
        appendNumbered(report, profile.answerCoaching());
        report.append("\n### 追问训练\n");
        report.append("- 如果面试官只关注 ").append(profile.coreKeywords().get(0)).append("，你如何用项目经历证明自己做过？\n");
        report.append("- 如果现有项目缺少 ").append(missing.isEmpty() ? profile.coreKeywords().get(1) : missing.get(0)).append("，你准备如何补齐证据？\n");
        return report.toString();
    }

    private List<String> mergeGaps(RoleProfile profile, List<String> detectedSkills, List<String> toolMissing) {
        List<String> gaps = new ArrayList<>();
        String skillText = String.join(" ", detectedSkills).toLowerCase(Locale.ROOT);
        for (String keyword : profile.coreKeywords()) {
            if (!skillText.contains(keyword.toLowerCase(Locale.ROOT))) {
                gaps.add(keyword);
            }
        }
        for (String item : toolMissing) {
            if (!gaps.contains(item)) {
                gaps.add(item);
            }
        }
        return gaps;
    }

    private void appendNumbered(StringBuilder report, List<String> items) {
        for (int i = 0; i < items.size(); i++) {
            report.append(i + 1).append(". ").append(items.get(i)).append("\n");
        }
    }

    private String resolveTargetPosition(Integer userId, String requestTargetPosition) {
        return seekerInfoRepository.findByUserId(userId)
                .map(info -> parseFavor(info.getFavor()))
                .filter(values -> !values.isEmpty())
                .map(values -> values.get(0))
                .filter(value -> !value.isBlank())
                .orElseGet(() -> requestTargetPosition == null || requestTargetPosition.isBlank()
                        ? "AI Agent 实习生"
                        : requestTargetPosition.trim());
    }

    private List<String> parseFavor(String favor) {
        if (favor == null || favor.isBlank()) {
            return List.of();
        }
        try {
            List<String> values = objectMapper.readValue(favor, new TypeReference<List<String>>() {});
            return values == null ? List.of() : values.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .toList();
        } catch (Exception ignored) {
            return List.of(favor.trim());
        }
    }

    private RoleProfile roleProfile(String targetPosition) {
        String target = targetPosition == null || targetPosition.isBlank() ? "AI Agent 实习生" : targetPosition.trim();
        String text = target.toLowerCase(Locale.ROOT);
        if (containsAny(text, "前端", "frontend", "vue", "react")) {
            return new RoleProfile(
                    target,
                    "前端开发",
                    List.of("Vue", "React", "TypeScript", "JavaScript", "CSS", "Vite", "Element Plus", "接口联调"),
                    List.of(
                            "整理组件化开发、状态管理、路由、表单校验和接口联调经历。",
                            "准备一个可演示页面，说明交互状态、异常处理和响应式布局。",
                            "补充性能优化证据，例如首屏加载、按需加载、组件复用或打包体积。",
                            "把项目中的 OfferAgent 页面改造成前端亮点：路由拆分、组件复用和统一 UI 风格。"
                    ),
                    List.of(
                            "把前端技术栈放到项目描述前半段，突出 Vue、Element Plus、Vite 和接口联调。",
                            "项目经历要写清楚页面结构、组件拆分、状态管理和异常反馈。",
                            "补充移动端适配、加载态、空状态、错误状态等用户体验细节。",
                            "用具体页面作为证据，例如工作流页面、智能助手对话页和菜单路由。"
                    ),
                    List.of(
                            "请介绍你如何设计 OfferAgent 多工作流页面的组件复用。",
                            "Vue 中你如何处理路由切换、页面状态重置和加载态？",
                            "如果接口返回很慢或失败，前端如何给用户明确反馈？",
                            "你如何保证不同页面 UI 风格统一？"
                    ),
                    List.of(
                            "先讲页面目标，再讲组件结构、数据流、接口调用和异常状态。",
                            "回答 Vue 问题时结合具体页面，不要只背概念。",
                            "准备一个组件复用例子，说明为什么没有复制三套页面。",
                            "说明你如何通过 build 验证路由和页面没有破坏。"
                    ),
                    "基于 Vue3、Element Plus 和 Vite 设计 OfferAgent 多工作流页面，将三个 Agent 流程抽象为统一组件，通过路由 meta 区分业务类型，并实现加载态、空状态、步骤展示和 Markdown 报告渲染。",
                    "前端工程化和交互体验"
            );
        }
        if (containsAny(text, "后端", "backend", "java", "spring")) {
            return new RoleProfile(
                    target,
                    "Java 后端开发",
                    List.of("Java", "Spring Boot", "JPA", "MySQL", "Redis", "REST API", "WebClient", "JUnit"),
                    List.of(
                            "梳理 Controller、Service、Repository、DTO 的分层职责。",
                            "准备接口设计说明，包括请求参数、返回结构、错误处理和权限边界。",
                            "补充数据库表、索引、日志表和事务一致性相关表达。",
                            "准备测试用例，说明如何验证工作流、工具调用和降级逻辑。"
                    ),
                    List.of(
                            "把 Spring Boot 分层、JPA 查询、WebClient 调用和测试覆盖写进项目描述。",
                            "突出工具调用日志、工作流接口和参数校验等后端工程能力。",
                            "补充数据库设计，例如用户表、简历表、知识库表、工具日志表。",
                            "说明接口如何被前端页面消费，体现前后端联调能力。"
                    ),
                    List.of(
                            "请介绍 OfferAgent 后端的 Controller-Service-Repository 调用链。",
                            "工具调用为什么必须在后端做白名单和参数校验？",
                            "你如何设计工作流接口的返回结构？",
                            "如果 ai-module 不可用，主流程如何降级？"
                    ),
                    List.of(
                            "回答时按接口入口、服务编排、数据访问、日志和测试的顺序展开。",
                            "强调确定性工具执行和 LLM 规划之间的安全边界。",
                            "准备一个接口返回 JSON 的例子，说明前端如何展示。",
                            "说明你用哪些单元测试覆盖关键分支。"
                    ),
                    "基于 Spring Boot 设计 OfferAgent 工作流接口和工具执行层，封装 Controller、Service、DTO 与日志查询能力，支持 RAG 检索、工具白名单校验、参数规整、fallback 和 JUnit 测试验证。",
                    "后端接口设计和工程可靠性"
            );
        }
        if (containsAny(text, "数据", "算法", "机器学习", "ml", "python", "分析")) {
            return new RoleProfile(
                    target,
                    "数据/算法",
                    List.of("Python", "SQL", "数据清洗", "特征工程", "模型评估", "Embedding", "召回率", "A/B 分析"),
                    List.of(
                            "整理数据处理、指标定义、模型评估和实验分析经历。",
                            "准备解释 RAG 检索的召回、相似度、重排和评估指标。",
                            "补充一个可量化案例，例如命中率、准确率、覆盖率或响应耗时。",
                            "把 OfferAgent 的检索链路讲成数据驱动的排序和评估问题。"
                    ),
                    List.of(
                            "把 SQL、数据清洗、特征工程和指标评估放到简历关键词区域。",
                            "项目描述要写清楚数据来源、处理流程、评价指标和结论。",
                            "补充 RAG 检索的向量化、相似度和混合排序逻辑。",
                            "用数字表达效果，不要只写完成了模型或分析。"
                    ),
                    List.of(
                            "你会如何评估 RAG 检索结果的质量？",
                            "Embedding 向量检索和关键词检索分别有什么优缺点？",
                            "如果用户反馈结果不相关，你会从哪些数据指标排查？",
                            "请介绍一个你做过的数据清洗或特征设计过程。"
                    ),
                    List.of(
                            "先定义目标指标，再讲数据处理、模型/算法、评估和迭代。",
                            "回答算法问题时要说清楚假设、输入输出和评价方式。",
                            "准备一个失败案例，说明如何通过数据定位问题。",
                            "把 RAG 项目讲成检索、排序和反馈闭环。"
                    ),
                    "围绕 OfferAgent RAG 检索链路设计文档切分、Embedding 表示、关键词与向量混合召回，并通过检索命中、相似度评分和日志追踪分析结果质量。",
                    "数据指标和检索质量评估"
            );
        }
        if (containsAny(text, "产品", "pm", "运营")) {
            return new RoleProfile(
                    target,
                    "产品/运营",
                    List.of("用户需求", "业务流程", "原型设计", "数据指标", "转化率", "用户反馈", "需求优先级", "竞品分析"),
                    List.of(
                            "梳理求职用户的核心场景：简历优化、岗位匹配、面试准备和行动计划。",
                            "准备说明为什么要把 OfferAgent 拆成三条工作流，而不是一个大聊天框。",
                            "补充页面入口、用户路径、转化指标和反馈闭环。",
                            "准备讲清楚 MVP、迭代范围和后续商业化空间。"
                    ),
                    List.of(
                            "突出需求分析、流程设计、用户路径和指标意识。",
                            "项目描述要写清楚你解决了哪个用户痛点，而不是只写技术实现。",
                            "补充产品决策依据，例如为什么设计三个独立工作流页面。",
                            "用用户结果表达价值：更快定位简历问题、更清楚准备面试。"
                    ),
                    List.of(
                            "你为什么把 OfferAgent 拆成智能助手、冲刺规划、简历优化和模拟面试？",
                            "这个功能的核心用户路径是什么？",
                            "你会用哪些指标衡量求职助手是否有效？",
                            "如果只能保留一个工作流，你会优先保留哪个？为什么？"
                    ),
                    List.of(
                            "先讲用户痛点，再讲解决方案、流程和指标。",
                            "强调功能边界和迭代优先级，而不是陷入技术细节。",
                            "准备一个用户使用前后的对比场景。",
                            "说明如何根据用户反馈优化工作流。"
                    ),
                    "基于求职辅导场景拆分 OfferAgent 产品路径，设计智能问答、实习冲刺规划、简历优化和模拟面试四个入口，降低用户从信息填写到行动建议的决策成本。",
                    "用户路径和业务转化"
            );
        }
        return new RoleProfile(
                target,
                "AI Agent 应用开发",
                List.of("Java", "Spring Boot", "RAG", "Tool Calling", "AI Agent", "Prompt", "VectorStore", "Embedding"),
                List.of(
                        "梳理 OfferAgent 项目主线：RAG、Tool Calling、VectorStore 和多 Agent 工作流分别解决什么问题。",
                        "把简历项目描述改成可量化表达，例如检索模式、工具数量、fallback 机制、日志追踪和测试覆盖。",
                        "准备面试回答：为什么要拆分 Agent、为什么工具必须由后端校验执行、异常时如何降级。",
                        "完整演示一次从用户画像到最终行动计划的流程，并能解释每个后端服务和接口。"
                ),
                List.of(
                        "在简历靠前位置明确 AI Agent、RAG、Tool Calling 和多 Agent 工作流关键词。",
                        "项目经历按“业务目标 + 技术设计 + 工程保障 + 输出结果”改写。",
                        "增加工程化证据，例如参数校验、日志追踪、fallback、测试覆盖和前端可视化。",
                        "把笼统的大模型调用改成具体实现：知识库、工具注册、规划接口和执行日志。"
                ),
                List.of(
                        "为什么 OfferAgent 需要 RAG，而不是直接把用户问题交给大模型？",
                        "Tool Calling 如何映射到确定性的业务接口，并避免模型越权调用？",
                        "多 Agent 工作流中每个 Agent 的边界是什么？为什么这样拆分？",
                        "如果 RAG 没有命中相关知识片段，系统应该如何降级？"
                ),
                List.of(
                        "先讲业务目标，再讲 RAG、Tool Calling 和多 Agent 的分工。",
                        "强调 LLM 负责规划，后端确定性工具负责执行和校验。",
                        "准备一个失败案例：检索无命中、非法工具调用或 AI 模块不可用。",
                        "用具体接口、表结构和测试结果证明这不是简单 prompt demo。"
                ),
                "设计并实现 OfferAgent 智能求职助手，集成 RAG 检索、LLM 工具规划、后端工具白名单校验和多 Agent 工作流编排，支持简历诊断、岗位匹配和模拟面试等完整求职场景。",
                "Agent 编排和工程安全边界"
        );
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
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

    private record RoleProfile(
            String target,
            String category,
            List<String> coreKeywords,
            List<String> sprintFocus,
            List<String> resumePriorities,
            List<String> interviewQuestions,
            List<String> answerCoaching,
            String rewriteExample,
            String demoAngle) {
    }

    private record WorkflowStepPlan(String agentName, List<OfferAgentPlannedToolCall> calls) {
    }
}
