package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentEvaluationMetric;
import com.se.EdgeHire.DTO.OfferAgentEvaluationRequest;
import com.se.EdgeHire.DTO.OfferAgentLlmJudgeResult;

import java.util.List;
import java.util.Optional;

public interface OfferAgentLlmJudgeClient {
    Optional<OfferAgentLlmJudgeResult> judge(
            OfferAgentEvaluationRequest request,
            List<OfferAgentEvaluationMetric> ruleMetrics,
            int ruleScore,
            String ruleGrade);
}
