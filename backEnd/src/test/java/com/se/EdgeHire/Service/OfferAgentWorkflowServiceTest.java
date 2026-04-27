package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentWorkflowResponse;
import com.se.EdgeHire.DTO.OfferAgentWorkflowStreamEvent;
import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import com.se.EdgeHire.Repository.SeekerInfoRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferAgentWorkflowServiceTest {

    @Test
    void runAiAgentSprintReturnsFiveAgentStepsAndFinalReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService(), fallbackReportGenerator(), emptySeekerInfoRepository());

        OfferAgentWorkflowResponse response = workflowService.runAiAgentSprint(
                7,
                "workflow-c1",
                "AI Agent \u5b9e\u4e60\u751f"
        );

        assertThat(response.getWorkflowName()).isEqualTo("AI Agent \u5b9e\u4e60\u51b2\u523a\u89c4\u5212");
        assertThat(response.getSteps())
                .extracting("agentName")
                .containsExactly("ProfileAgent", "ResumeAgent", "KnowledgeAgent", "MatchAgent", "PlannerAgent");
        assertThat(response.getSteps()).allMatch(step -> "success".equals(step.getStatus()));
        assertThat(response.getFinalReport())
                .contains("\u5c97\u4f4d\u65b9\u5411\uff1aAI Agent \u5e94\u7528\u5f00\u53d1")
                .contains("\u5c97\u4f4d\u5339\u914d\u5ea6\uff1a83/100")
                .contains("Java\u3001Spring Boot\u3001RAG\u3001Tool Calling")
                .contains("Agent Memory");
    }

    @Test
    void runResumeOptimizationReturnsFiveAgentStepsAndRewriteReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService(), fallbackReportGenerator(), emptySeekerInfoRepository());

        OfferAgentWorkflowResponse response = workflowService.runResumeOptimization(
                7,
                "workflow-c2",
                "AI Agent \u5b9e\u4e60\u751f"
        );

        assertThat(response.getWorkflowName()).isEqualTo("\u7b80\u5386\u4f18\u5316\u5de5\u4f5c\u6d41");
        assertThat(response.getSteps())
                .extracting("agentName")
                .containsExactly("ResumeParserAgent", "JDAnalysisAgent", "ResumeCoachAgent", "RAGAgent", "RewriteAgent");
        assertThat(response.getSteps()).allMatch(step -> "success".equals(step.getStatus()));
        assertThat(response.getFinalReport())
                .contains("\u7b80\u5386\u4f18\u5316\u62a5\u544a")
                .contains("\u5c97\u4f4d\u65b9\u5411\uff1aAI Agent \u5e94\u7528\u5f00\u53d1")
                .contains("\u5df2\u8986\u76d6\u6280\u672f\u5173\u952e\u8bcd\uff1aJava\u3001Spring Boot\u3001RAG\u3001Tool Calling")
                .contains("\u8bbe\u8ba1\u5e76\u5b9e\u73b0 OfferAgent \u667a\u80fd\u6c42\u804c\u52a9\u624b");
    }

    @Test
    void runMockInterviewReturnsFiveAgentStepsAndCoachingReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService(), fallbackReportGenerator(), emptySeekerInfoRepository());

        OfferAgentWorkflowResponse response = workflowService.runMockInterview(
                7,
                "workflow-c3",
                "AI Agent \u5b9e\u4e60\u751f"
        );

        assertThat(response.getWorkflowName()).isEqualTo("\u6a21\u62df\u9762\u8bd5\u5de5\u4f5c\u6d41");
        assertThat(response.getSteps())
                .extracting("agentName")
                .containsExactly("ProfileAgent", "InterviewQuestionAgent", "EvaluatorAgent", "KnowledgeAgent", "CoachAgent");
        assertThat(response.getSteps()).allMatch(step -> "success".equals(step.getStatus()));
        assertThat(response.getFinalReport())
                .contains("\u9762\u8bd5\u51c6\u5907\u5ea6\uff1a83/100")
                .contains("\u9762\u8bd5\u9898\u6e05\u5355")
                .contains("\u4e3a\u4ec0\u4e48 OfferAgent \u9700\u8981 RAG")
                .contains("\u7b54\u9898\u5efa\u8bae");
    }

    @Test
    void targetPositionChangesReportContent() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService(), fallbackReportGenerator(), emptySeekerInfoRepository());

        OfferAgentWorkflowResponse frontend = workflowService.runResumeOptimization(
                7,
                "workflow-c4",
                "\u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f"
        );
        OfferAgentWorkflowResponse backend = workflowService.runResumeOptimization(
                7,
                "workflow-c5",
                "Java \u540e\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f"
        );

        assertThat(frontend.getFinalReport())
                .contains("\u5c97\u4f4d\u65b9\u5411\uff1a\u524d\u7aef\u5f00\u53d1")
                .contains("Vue\u3001React\u3001TypeScript")
                .contains("\u7ec4\u4ef6\u62c6\u5206")
                .doesNotContain("\u5c97\u4f4d\u65b9\u5411\uff1aAI Agent \u5e94\u7528\u5f00\u53d1");
        assertThat(backend.getFinalReport())
                .contains("\u5c97\u4f4d\u65b9\u5411\uff1aJava \u540e\u7aef\u5f00\u53d1")
                .contains("Spring Boot\u3001JPA\u3001MySQL")
                .contains("Controller\u3001Service\u3001DTO");
        assertThat(frontend.getFinalReport()).isNotEqualTo(backend.getFinalReport());
    }

    @Test
    void workflowPrefersDatabaseTargetPositionOverRequestValue() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(
                executionService(),
                fallbackReportGenerator(),
                seekerInfoRepositoryWithFavor("[\"前端开发实习生\"]")
        );

        OfferAgentWorkflowResponse response = workflowService.runAiAgentSprint(
                7,
                "workflow-db-target",
                "AI Agent 实习生"
        );

        assertThat(response.getTargetPosition()).isEqualTo("前端开发实习生");
        assertThat(response.getFinalReport())
                .contains("岗位方向：前端开发")
                .doesNotContain("岗位方向：AI Agent 应用开发");
    }


    @Test
    void workflowUsesLlmReportGeneratorForFinalReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(
                executionService(),
                (conversationId, workflowName, targetPosition, steps, fallbackReport) ->
                        "\u5927\u6a21\u578b\u5b9e\u65f6\u62a5\u544a\uff1a" + workflowName + " / " + targetPosition + " / steps=" + steps.size(),
                emptySeekerInfoRepository()
        );

        OfferAgentWorkflowResponse response = workflowService.runMockInterview(
                7,
                "workflow-c6",
                "\u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f"
        );

        assertThat(response.getFinalReport())
                .isEqualTo("\u5927\u6a21\u578b\u5b9e\u65f6\u62a5\u544a\uff1a\u6a21\u62df\u9762\u8bd5\u5de5\u4f5c\u6d41 / \u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f / steps=5");
    }

    @Test
    void streamWorkflowEmitsStepAndReportEvents() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService(), fallbackReportGenerator(), emptySeekerInfoRepository());

        List<OfferAgentWorkflowStreamEvent> events = workflowService.streamResumeOptimization(
                        7,
                        "workflow-stream-c1",
                        "\u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f"
                )
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(events).isNotNull();
        assertThat(events)
                .extracting(OfferAgentWorkflowStreamEvent::getType)
                .contains("workflow_start", "step_start", "step_complete", "report_start", "report_delta", "workflow_done");
        assertThat(events.stream().filter(event -> "step_complete".equals(event.getType())).count()).isEqualTo(5);
        assertThat(events.stream()
                .filter(event -> "report_delta".equals(event.getType()))
                .map(OfferAgentWorkflowStreamEvent::getContent)
                .findFirst()
                .orElse(""))
                .contains("\u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f");
    }

    private OfferAgentToolExecutionService executionService() {
        OfferAgentToolRegistry registry = new OfferAgentToolRegistry(List.of(
                fakeTool("get_user_profile", Map.of("summary", "profile loaded")),
                fakeTool("get_job_intention", Map.of("summary", "intention loaded")),
                fakeTool("get_resume_summary", Map.of(
                        "summary", "resume loaded",
                        "detectedSkills", List.of("Java", "Spring Boot", "RAG", "Tool Calling")
                )),
                fakeTool("retrieve_knowledge", Map.of("summary", "knowledge retrieved", "hitCount", 5)),
                fakeTool("calculate_job_match_score", Map.of(
                        "summary", "match score=83",
                        "score", 83,
                        "missingKeywords", List.of("Agent Memory", "Evaluation")
                )),
                fakeTool("generate_interview_plan", Map.of(
                        "summary", "interview plan generated",
                        "questions", List.of(
                                "\u8bf7\u4ecb\u7ecd OfferAgent \u67b6\u6784\u548c\u6570\u636e\u6d41\u3002",
                                "\u4f60\u5982\u4f55\u4fdd\u8bc1 Tool Calling \u7684\u5b89\u5168\u6027\uff1f",
                                "\u4f60\u4f1a\u5982\u4f55\u8bc4\u4f30 RAG \u68c0\u7d22\u8d28\u91cf\uff1f"
                        )
                ))
        ));
        return new OfferAgentToolExecutionService(
                new OfferAgentToolPlanner(),
                mock(LlmOfferAgentToolPlanner.class),
                registry,
                mock(OfferAgentToolCallLogRepository.class)
        );
    }

    private OfferAgentWorkflowReportGenerator fallbackReportGenerator() {
        return (conversationId, workflowName, targetPosition, steps, fallbackReport) -> fallbackReport;
    }

    private SeekerInfoRepository emptySeekerInfoRepository() {
        SeekerInfoRepository repository = mock(SeekerInfoRepository.class);
        when(repository.findByUserId(7)).thenReturn(Optional.empty());
        return repository;
    }

    private SeekerInfoRepository seekerInfoRepositoryWithFavor(String favor) {
        SeekerInfo seekerInfo = new SeekerInfo();
        seekerInfo.setUserId(7);
        seekerInfo.setFavor(favor);
        SeekerInfoRepository repository = mock(SeekerInfoRepository.class);
        when(repository.findByUserId(7)).thenReturn(Optional.of(seekerInfo));
        return repository;
    }

    private OfferAgentTool fakeTool(String name, Map<String, Object> output) {
        return new OfferAgentTool() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return "fake " + name;
            }

            @Override
            public Map<String, Object> execute(OfferAgentToolContext context) {
                return output;
            }
        };
    }
}
