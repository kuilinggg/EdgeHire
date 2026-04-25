package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentWorkflowResponse;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OfferAgentWorkflowServiceTest {

    @Test
    void runAiAgentSprintReturnsFiveAgentStepsAndFinalReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService());

        OfferAgentWorkflowResponse response = workflowService.runAiAgentSprint(
                7,
                "workflow-c1",
                "AI Agent intern"
        );

        assertThat(response.getWorkflowName()).isEqualTo("AI Agent Internship Sprint Workflow");
        assertThat(response.getSteps())
                .extracting("agentName")
                .containsExactly("ProfileAgent", "ResumeAgent", "KnowledgeAgent", "MatchAgent", "PlannerAgent");
        assertThat(response.getSteps()).allMatch(step -> "success".equals(step.getStatus()));
        assertThat(response.getFinalReport())
                .contains("Match score: 83/100")
                .contains("Java, Spring Boot, RAG, Tool Calling")
                .contains("Agent Memory, Evaluation");
    }

    @Test
    void runResumeOptimizationReturnsFiveAgentStepsAndRewriteReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService());

        OfferAgentWorkflowResponse response = workflowService.runResumeOptimization(
                7,
                "workflow-c2",
                "AI Agent intern"
        );

        assertThat(response.getWorkflowName()).isEqualTo("Resume Optimization Workflow");
        assertThat(response.getSteps())
                .extracting("agentName")
                .containsExactly("ResumeParserAgent", "JDAnalysisAgent", "ResumeCoachAgent", "RAGAgent", "RewriteAgent");
        assertThat(response.getSteps()).allMatch(step -> "success".equals(step.getStatus()));
        assertThat(response.getFinalReport())
                .contains("Current resume match score: 83/100")
                .contains("Existing technical keywords: Java, Spring Boot, RAG, Tool Calling")
                .contains("Example rewrite direction")
                .contains("Designed OfferAgent with RAG retrieval");
    }

    @Test
    void runMockInterviewReturnsFiveAgentStepsAndCoachingReport() {
        OfferAgentWorkflowService workflowService = new OfferAgentWorkflowService(executionService());

        OfferAgentWorkflowResponse response = workflowService.runMockInterview(
                7,
                "workflow-c3",
                "AI Agent intern"
        );

        assertThat(response.getWorkflowName()).isEqualTo("Mock Interview Workflow");
        assertThat(response.getSteps())
                .extracting("agentName")
                .containsExactly("ProfileAgent", "InterviewQuestionAgent", "EvaluatorAgent", "KnowledgeAgent", "CoachAgent");
        assertThat(response.getSteps()).allMatch(step -> "success".equals(step.getStatus()));
        assertThat(response.getFinalReport())
                .contains("Interview readiness score: 83/100")
                .contains("Interview question set")
                .contains("Explain OfferAgent architecture")
                .contains("Answer coaching");
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
                                "Explain OfferAgent architecture and data flow.",
                                "How do you make Tool Calling safe?",
                                "How would you evaluate RAG retrieval quality?"
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
