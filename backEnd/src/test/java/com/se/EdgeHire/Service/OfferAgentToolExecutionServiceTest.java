package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolPlan;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferAgentToolExecutionServiceTest {

    @Test
    void executeFallsBackToRulePlannerAndReturnsDeterministicResults() {
        OfferAgentToolRegistry registry = new OfferAgentToolRegistry(List.of(
                fakeTool("get_user_profile"),
                fakeTool("get_job_intention"),
                fakeTool("get_resume_summary"),
                fakeTool("retrieve_knowledge"),
                fakeTool("calculate_job_match_score"),
                fakeTool("generate_interview_plan")
        ));
        LlmOfferAgentToolPlanner llmPlanner = mock(LlmOfferAgentToolPlanner.class);
        when(llmPlanner.plan(eq("c1"), anyString(), eq(""))).thenReturn(Optional.empty());
        OfferAgentToolExecutionService service = new OfferAgentToolExecutionService(
                new OfferAgentToolPlanner(),
                llmPlanner,
                registry,
                mock(OfferAgentToolCallLogRepository.class)
        );

        List<OfferAgentToolResult> results = service.execute(
                7,
                "c1",
                "I want an AI Agent internship match score and interview plan."
        );

        assertThat(results).hasSize(6);
        assertThat(results).allMatch(OfferAgentToolResult::getSuccess);
        assertThat(results).allMatch(result -> "rule_fallback".equals(result.getPlanSource()));
        assertThat(results)
                .extracting(OfferAgentToolResult::getToolName)
                .contains("calculate_job_match_score", "generate_interview_plan");
    }

    @Test
    void executeUsesValidLlmPlanBeforeRuleFallback() {
        OfferAgentToolRegistry registry = new OfferAgentToolRegistry(List.of(
                fakeTool("retrieve_knowledge"),
                fakeTool("calculate_job_match_score")
        ));
        LlmOfferAgentToolPlanner llmPlanner = mock(LlmOfferAgentToolPlanner.class);
        when(llmPlanner.plan("c2", "plan my AI Agent interview", "ctx"))
                .thenReturn(Optional.of(new OfferAgentToolPlan(
                        "llm",
                        null,
                        List.of(
                                new OfferAgentPlannedToolCall("retrieve_knowledge", Map.of("query", "AI Agent interview")),
                                new OfferAgentPlannedToolCall("unknown_tool", Map.of()),
                                new OfferAgentPlannedToolCall("calculate_job_match_score", Map.of(
                                        "targetPosition", "AI Agent intern",
                                        "ignored", "value"
                                ))
                        )
                )));

        OfferAgentToolExecutionService service = new OfferAgentToolExecutionService(
                new OfferAgentToolPlanner(),
                llmPlanner,
                registry,
                mock(OfferAgentToolCallLogRepository.class)
        );

        List<OfferAgentToolResult> results = service.execute(7, "c2", "plan my AI Agent interview", "ctx");

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(result -> "llm".equals(result.getPlanSource()));
        assertThat(results)
                .extracting(OfferAgentToolResult::getToolName)
                .containsExactly("retrieve_knowledge", "calculate_job_match_score");
        assertThat(results.get(0).getInputJson()).contains("\"topK\":5");
        assertThat(results.get(1).getInputJson()).doesNotContain("ignored");
    }

    @Test
    void executeWithReportReturnsTraceAndClampsToolArguments() {
        OfferAgentToolRegistry registry = new OfferAgentToolRegistry(List.of(
                fakeTool("retrieve_knowledge"),
                fakeTool("generate_interview_plan")
        ));
        LlmOfferAgentToolPlanner llmPlanner = mock(LlmOfferAgentToolPlanner.class);
        when(llmPlanner.plan("c3", "prepare", "ctx"))
                .thenReturn(Optional.of(new OfferAgentToolPlan(
                        "llm",
                        null,
                        List.of(
                                new OfferAgentPlannedToolCall("retrieve_knowledge", Map.of("query", "AI Agent", "topK", 99)),
                                new OfferAgentPlannedToolCall("generate_interview_plan", Map.of("targetPosition", "AI Agent intern", "days", 30))
                        )
                )));

        OfferAgentToolExecutionService service = new OfferAgentToolExecutionService(
                new OfferAgentToolPlanner(),
                llmPlanner,
                registry,
                mock(OfferAgentToolCallLogRepository.class)
        );

        OfferAgentToolExecutionReport report = service.executeWithReport(7, "c3", "prepare", "ctx");

        assertThat(report.getPlanSource()).isEqualTo("llm");
        assertThat(report.getTrace()).contains("tool_plan_source=llm", "validated_tool_calls=2");
        assertThat(report.getToolCalls()).hasSize(2);
        assertThat(report.getToolCalls().get(0).getInputJson()).contains("\"topK\":8");
        assertThat(report.getToolCalls().get(1).getInputJson()).contains("\"days\":14");
    }

    private OfferAgentTool fakeTool(String name) {
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
                return Map.of("summary", name + " executed", "userId", context.getUserId());
            }
        };
    }
}
