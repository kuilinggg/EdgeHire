package com.se.EdgeHire.Service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfferAgentToolPlannerTest {

    @Test
    void planSelectsMatchAndInterviewToolsForTargetRoleQuestion() {
        OfferAgentToolPlanner planner = new OfferAgentToolPlanner();

        List<String> tools = planner.plan("我想投 AI Agent 实习岗，请判断匹配度，并准备面试");

        assertThat(tools).contains(
                "get_user_profile",
                "get_job_intention",
                "get_resume_summary",
                "retrieve_knowledge",
                "calculate_job_match_score",
                "generate_interview_plan"
        );

        assertThat(planner.planCalls("plan my AI Agent interview"))
                .extracting("toolName")
                .contains("retrieve_knowledge", "generate_interview_plan");
    }
}
