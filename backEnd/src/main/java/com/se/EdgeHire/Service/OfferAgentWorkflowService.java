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
    private static final String SPRINT_WORKFLOW_NAME = "AI Agent Internship Sprint Workflow";
    private static final String RESUME_WORKFLOW_NAME = "Resume Optimization Workflow";
    private static final String MOCK_INTERVIEW_WORKFLOW_NAME = "Mock Interview Workflow";

    private final OfferAgentToolExecutionService toolExecutionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentWorkflowResponse runAiAgentSprint(Integer userId, String conversationId, String targetPosition) {
        String target = targetPosition == null || targetPosition.isBlank()
                ? "AI Agent intern"
                : targetPosition.trim();
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-ai-agent-sprint-" + userId
                : conversationId;
        String message = "Run AI Agent internship sprint workflow for target position: " + target;

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ProfileAgent", List.of(
                call("get_user_profile"),
                call("get_job_intention")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "ResumeAgent", List.of(
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "KnowledgeAgent", List.of(
                call("retrieve_knowledge", Map.of("query", target + " RAG Tool Calling AI Agent interview resume", "topK", 5))
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
                ? "AI Agent intern"
                : targetPosition.trim();
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-resume-optimization-" + userId
                : conversationId;
        String message = "Run resume optimization workflow for target position: " + target;

        List<OfferAgentWorkflowStep> steps = new ArrayList<>();
        steps.add(runStep(userId, workflowConversationId, message, "ResumeParserAgent", List.of(
                call("get_resume_summary")
        )));
        steps.add(runStep(userId, workflowConversationId, message, "JDAnalysisAgent", List.of(
                call("get_job_intention"),
                call("retrieve_knowledge", Map.of("query", target + " job description skill keywords resume screening", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "ResumeCoachAgent", List.of(
                call("calculate_job_match_score", Map.of("targetPosition", target))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "RAGAgent", List.of(
                call("retrieve_knowledge", Map.of("query", target + " resume optimization STAR quantified project experience", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "RewriteAgent", List.of(
                call("get_resume_summary"),
                call("retrieve_knowledge", Map.of("query", "resume rewrite examples RAG Tool Calling AI Agent project bullets", "topK", 3))
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
                ? "AI Agent intern"
                : targetPosition.trim();
        String workflowConversationId = conversationId == null || conversationId.isBlank()
                ? "workflow-mock-interview-" + userId
                : conversationId;
        String message = "Run mock interview workflow for target position: " + target;

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
                call("retrieve_knowledge", Map.of("query", target + " mock interview RAG Tool Calling answer framework follow-up questions", "topK", 5))
        )));
        steps.add(runStep(userId, workflowConversationId, message, "CoachAgent", List.of(
                call("retrieve_knowledge", Map.of("query", "AI Agent interview answer improvement STAR project explanation", "topK", 3)),
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
        String joined = results.stream()
                .map(result -> result.getSummary() == null ? result.getToolName() : result.getSummary())
                .reduce((left, right) -> left + "; " + right)
                .orElse("no tool result");
        return agentName + " completed: " + joined;
    }

    private String buildSprintFinalReport(String target, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = findStringArray(steps, "calculate_job_match_score", "missingKeywords");
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## AI Agent Internship Sprint Report\n\n");
        report.append("- Target position: ").append(target).append("\n");
        report.append("- Workflow agents: ProfileAgent, ResumeAgent, KnowledgeAgent, MatchAgent, PlannerAgent\n");
        if (score != null) {
            report.append("- Match score: ").append(score).append("/100\n");
        }
        if (!skills.isEmpty()) {
            report.append("- Resume keywords detected: ").append(String.join(", ", skills)).append("\n");
        }
        if (!missing.isEmpty()) {
            report.append("- Priority gaps: ").append(String.join(", ", missing.subList(0, Math.min(5, missing.size())))).append("\n");
        }
        if (hitCount != null) {
            report.append("- RAG knowledge hits: ").append(hitCount).append("\n");
        }
        report.append("\n### 7-day sprint focus\n");
        report.append("1. Clarify the project story around RAG, Tool Calling, VectorStore, and multi-agent workflow.\n");
        report.append("2. Add measurable resume bullets for retrieval quality, tool safety, fallback, and workflow traceability.\n");
        report.append("3. Prepare interview answers for why each Agent exists and how deterministic tools control risk.\n");
        report.append("4. Run a full demo from user profile to final action plan and explain each backend step.\n");
        return report.toString();
    }

    private String buildResumeOptimizationReport(String target, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = findStringArray(steps, "calculate_job_match_score", "missingKeywords");
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## Resume Optimization Report\n\n");
        report.append("- Target position: ").append(target).append("\n");
        report.append("- Workflow agents: ResumeParserAgent, JDAnalysisAgent, ResumeCoachAgent, RAGAgent, RewriteAgent\n");
        if (score != null) {
            report.append("- Current resume match score: ").append(score).append("/100\n");
        }
        if (!skills.isEmpty()) {
            report.append("- Existing technical keywords: ").append(String.join(", ", skills)).append("\n");
        }
        if (!missing.isEmpty()) {
            report.append("- Keywords or evidence to strengthen: ")
                    .append(String.join(", ", missing.subList(0, Math.min(5, missing.size()))))
                    .append("\n");
        }
        if (hitCount != null) {
            report.append("- Resume knowledge hits: ").append(hitCount).append("\n");
        }
        report.append("\n### Optimization priorities\n");
        report.append("1. Put the target role, core AI Agent keywords, and project outcome near the top of the resume.\n");
        report.append("2. Rewrite project bullets with Action + Technical Design + Result, especially RAG, Tool Calling, VectorStore, and workflow traceability.\n");
        report.append("3. Add measurable engineering evidence such as test coverage, latency, retrieval mode, fallback behavior, or number of tools/agents integrated.\n");
        report.append("4. Move vague descriptions into concrete implementation details: data tables, endpoints, services, and safety checks.\n");
        report.append("\n### Example rewrite direction\n");
        report.append("- Before: Built an AI job-search assistant.\n");
        report.append("- After: Designed OfferAgent with RAG retrieval, LLM tool planning, backend tool whitelist validation, and multi-agent workflow orchestration to generate resume diagnosis, role match analysis, and interview preparation plans.\n");
        return report.toString();
    }

    private String buildMockInterviewReport(String target, List<OfferAgentWorkflowStep> steps) {
        List<String> skills = findStringArray(steps, "get_resume_summary", "detectedSkills");
        Integer score = findInt(steps, "calculate_job_match_score", "score");
        List<String> missing = findStringArray(steps, "calculate_job_match_score", "missingKeywords");
        List<String> questions = findStringArray(steps, "generate_interview_plan", "questions");
        Integer hitCount = findInt(steps, "retrieve_knowledge", "hitCount");

        StringBuilder report = new StringBuilder();
        report.append("## Mock Interview Report\n\n");
        report.append("- Target position: ").append(target).append("\n");
        report.append("- Workflow agents: ProfileAgent, InterviewQuestionAgent, EvaluatorAgent, KnowledgeAgent, CoachAgent\n");
        if (score != null) {
            report.append("- Interview readiness score: ").append(score).append("/100\n");
        }
        if (!skills.isEmpty()) {
            report.append("- Resume-backed topics: ").append(String.join(", ", skills)).append("\n");
        }
        if (!missing.isEmpty()) {
            report.append("- Follow-up risk areas: ")
                    .append(String.join(", ", missing.subList(0, Math.min(5, missing.size()))))
                    .append("\n");
        }
        if (hitCount != null) {
            report.append("- Interview knowledge hits: ").append(hitCount).append("\n");
        }
        report.append("\n### Interview question set\n");
        if (questions.isEmpty()) {
            report.append("1. Explain the architecture of your OfferAgent project and why it needs RAG.\n");
            report.append("2. How does your Tool Calling layer prevent unsafe model outputs from touching business data?\n");
            report.append("3. What does each Agent do in your multi-agent workflow, and why split them this way?\n");
        } else {
            for (int i = 0; i < Math.min(5, questions.size()); i++) {
                report.append(i + 1).append(". ").append(questions.get(i)).append("\n");
            }
        }
        report.append("\n### Answer coaching\n");
        report.append("1. Start with business goal, then explain architecture, data flow, and safety controls.\n");
        report.append("2. For every project feature, prepare one concrete endpoint, service class, table, and test case.\n");
        report.append("3. When asked about Agent design, contrast deterministic tools with LLM planning and explain fallback.\n");
        report.append("4. Prepare one failure case: retrieval miss, invalid tool call, or incomplete resume data, then explain degradation.\n");
        report.append("\n### Suggested follow-up drill\n");
        report.append("- Ask yourself: if the interviewer removes the LLM, which parts still work and why?\n");
        report.append("- Ask yourself: how would you evaluate retrieval quality and tool-call correctness?\n");
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
