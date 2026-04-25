package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentPlannedToolCall;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    public List<OfferAgentPlannedToolCall> planCalls(String message) {
        return plan(message).stream()
                .map(toolName -> new OfferAgentPlannedToolCall(toolName, defaultArguments(toolName, message)))
                .toList();
    }

    private LinkedHashMap<String, Object> defaultArguments(String toolName, String message) {
        LinkedHashMap<String, Object> arguments = new LinkedHashMap<>();
        if ("retrieve_knowledge".equals(toolName)) {
            arguments.put("query", message == null ? "" : message);
            arguments.put("topK", 5);
        }
        if ("calculate_job_match_score".equals(toolName) || "generate_interview_plan".equals(toolName)) {
            arguments.put("targetPosition", inferTargetPosition(message));
        }
        if ("generate_interview_plan".equals(toolName)) {
            arguments.put("days", 7);
        }
        return arguments;
    }

    private String inferTargetPosition(String message) {
        String text = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (text.contains("agent")) {
            return "AI Agent 实习生";
        }
        return "用户问题中的目标岗位";
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
