package com.se.ai_module.service;

import com.se.ai_module.dto.OfferAgentChatRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OfferAgentPromptBuilderTest {

    @Test
    void buildCreatesMultiAgentPromptWithRetrievedContext() {
        OfferAgentChatRequest request = new OfferAgentChatRequest();
        request.setConversationId("c1");
        request.setMessage("I want an AI Agent internship plan.");
        request.setUserContext("getResumeTool: loaded 1 resume");

        String prompt = new OfferAgentPromptBuilder().build(request);

        assertThat(prompt)
                .contains("ProfileAgent")
                .contains("ResumeCoachAgent")
                .contains("JobMatchAgent")
                .contains("InterviewAgent")
                .contains("PlannerAgent")
                .contains("getResumeTool: loaded 1 resume")
                .contains("I want an AI Agent internship plan.");
    }
}
