package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentEvaluationResponse {
    private Integer overallScore;
    private String grade;
    private Integer ruleScore;
    private String ruleGrade;
    private Integer llmJudgeScore;
    private String llmJudgeGrade;
    private String evaluationMode;
    private OfferAgentLlmJudgeResult llmJudge;
    private List<OfferAgentEvaluationMetric> metrics = new ArrayList<>();
    private List<String> strengths = new ArrayList<>();
    private List<String> risks = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();

    public OfferAgentEvaluationResponse(
            Integer overallScore,
            String grade,
            List<OfferAgentEvaluationMetric> metrics,
            List<String> strengths,
            List<String> risks,
            List<String> suggestions) {
        this.overallScore = overallScore;
        this.grade = grade;
        this.ruleScore = overallScore;
        this.ruleGrade = grade;
        this.llmJudgeScore = null;
        this.llmJudgeGrade = null;
        this.evaluationMode = "rule_only";
        this.llmJudge = OfferAgentLlmJudgeResult.unavailable("LLM judge was not requested.");
        this.metrics = metrics;
        this.strengths = strengths;
        this.risks = risks;
        this.suggestions = suggestions;
    }
}
