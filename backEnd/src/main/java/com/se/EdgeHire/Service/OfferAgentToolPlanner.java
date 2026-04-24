package com.se.EdgeHire.Service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class OfferAgentToolPlanner {
    public List<String> plan(String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);
        Set<String> tools = new LinkedHashSet<>();

        tools.add("get_user_profile");
        tools.add("get_job_intention");
        tools.add("get_resume_summary");
        tools.add("retrieve_knowledge");

        if (containsAny(text, "match", "score", "fit", "gap", "适合", "匹配", "差距", "岗位", "投")) {
            tools.add("calculate_job_match_score");
        }
        if (containsAny(text, "interview", "prepare", "question", "面试", "准备", "题", "模拟")) {
            tools.add("generate_interview_plan");
        }

        return new ArrayList<>(tools);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
