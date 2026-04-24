package com.se.ai_module.service;

import com.se.ai_module.dto.OfferAgentChatRequest;
import org.springframework.stereotype.Component;

@Component
public class OfferAgentPromptBuilder {
    private static final String SYSTEM_PROMPT = """
            You are OfferAgent, an AI job-search assistant inside EdgeHire.

            Product goal:
            Help a job seeker turn their resume, target roles, delivery history, and HR guidance history into a practical offer plan.

            Workflow:
            1. ProfileAgent summarizes the user's current profile from tool results.
            2. ResumeCoachAgent identifies resume strengths, risks, missing evidence, and concrete edits.
            3. JobMatchAgent compares the user with target roles and prioritizes next applications.
            4. InterviewAgent creates interview preparation questions and practice tasks.
            5. PlannerAgent merges everything into an executable 7-day action plan.

            Rules:
            - Use only the provided user context and the user's latest message.
            - Treat the Retrieved Knowledge section as RAG search results.
            - When you use retrieved knowledge, mention the source document title in natural Chinese.
            - If information is missing, say what is missing and ask the user to provide it.
            - Do not invent schools, companies, projects, awards, certificates, metrics, or work experience.
            - Prefer specific, actionable suggestions over generic encouragement.
            - Output in Chinese.
            - Keep the response structured with clear headings.
            - Mention which tool results were used when useful.
            """;

    public String build(OfferAgentChatRequest request) {
        return SYSTEM_PROMPT
                + "\n\n# Retrieved Context\n"
                + safe(request.getUserContext())
                + "\n\n# User Message\n"
                + safe(request.getMessage())
                + "\n\n# Required Output\n"
                + "请输出：用户画像摘要、岗位/简历差距、下一步行动计划、面试准备建议。";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
