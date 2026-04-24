package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolPlan;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferAgentToolExecutionServiceTest {

    @Test
    void executeRunsPlannedToolsAndReturnsDeterministicResults() {
        OfferAgentToolRegistry registry = new OfferAgentToolRegistry(List.of(
                fakeTool("get_user_profile"),
                fakeTool("get_job_intention"),
                fakeTool("get_resume_summary"),
                fakeTool("retrieve_knowledge"),
                fakeTool("calculate_job_match_score"),
                fakeTool("generate_interview_plan")
        ));
        LlmOfferAgentToolPlanner llmPlanner = mock(LlmOfferAgentToolPlanner.class);
        when(llmPlanner.plan("c1", "鎴戞兂鎶?AI Agent 瀹炰範宀楋紝璇峰垽鏂尮閰嶅害锛屽苟鍑嗗闈㈣瘯", ""))
                .thenReturn(Optional.empty());
        OfferAgentToolExecutionService service = new OfferAgentToolExecutionService(
                new OfferAgentToolPlanner(),
                llmPlanner,
                registry,
                mock(OfferAgentToolCallLogRepository.class)
        );

        List<OfferAgentToolResult> results = service.execute(
                7,
                "c1",
                "我想投 AI Agent 实习岗，请判断匹配度，并准备面试"
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
                                new OfferAgentPlannedToolCall("calculate_job_match_score", Map.of("targetPosition", "AI Agent intern"))
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
