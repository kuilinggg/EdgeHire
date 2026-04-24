package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
        OfferAgentToolExecutionService service = new OfferAgentToolExecutionService(
                new OfferAgentToolPlanner(),
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
        assertThat(results)
                .extracting(OfferAgentToolResult::getToolName)
                .contains("calculate_job_match_score", "generate_interview_plan");
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
