package com.se.ai_module.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfferAgentLlmJudgeResult {
    private Boolean available = true;
    private Integer score = 0;
    private String grade = "E";
    private String summary = "";
    private List<String> strengths = new ArrayList<>();
    private List<String> risks = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private String rawResponse;
    private String fallbackReason;

    public static OfferAgentLlmJudgeResult unavailable(String reason) {
        OfferAgentLlmJudgeResult result = new OfferAgentLlmJudgeResult();
        result.setAvailable(false);
        result.setGrade("N/A");
        result.setSummary("LLM 裁判暂不可用，已使用规则评测结果。");
        result.setFallbackReason(reason);
        return result;
    }
}
