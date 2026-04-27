package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentLlmJudgeResult {
    private Boolean available;
    private Integer score;
    private String grade;
    private String summary;
    private List<String> strengths = new ArrayList<>();
    private List<String> risks = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private String rawResponse;
    private String fallbackReason;

    public static OfferAgentLlmJudgeResult unavailable(String reason) {
        OfferAgentLlmJudgeResult result = new OfferAgentLlmJudgeResult();
        result.setAvailable(false);
        result.setScore(0);
        result.setGrade("N/A");
        result.setSummary("LLM 裁判暂不可用，已使用规则评测结果。");
        result.setFallbackReason(reason);
        return result;
    }
}
