package com.se.ai_module.service;

import com.se.ai_module.dto.OfferAgentToolPlan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OfferAgentToolPlanParserTest {

    @Test
    void parseExtractsToolCallsFromJsonOnlyResponse() {
        OfferAgentToolPlan plan = new OfferAgentToolPlanParser().parse("""
                {"toolCalls":[
                  {"toolName":"retrieve_knowledge","arguments":{"query":"AI Agent interview","topK":5}},
                  {"toolName":"calculate_job_match_score","arguments":{"targetPosition":"AI Agent intern"}}
                ]}
                """);

        assertThat(plan.getSource()).isEqualTo("llm");
        assertThat(plan.getToolCalls()).hasSize(2);
        assertThat(plan.getToolCalls().get(0).getToolName()).isEqualTo("retrieve_knowledge");
        assertThat(plan.getToolCalls().get(0).getArguments()).containsEntry("query", "AI Agent interview");
    }

    @Test
    void parseReturnsEmptyPlanForInvalidJson() {
        OfferAgentToolPlan plan = new OfferAgentToolPlanParser().parse("not json");

        assertThat(plan.getSource()).isEqualTo("llm");
        assertThat(plan.getToolCalls()).isEmpty();
    }
}
