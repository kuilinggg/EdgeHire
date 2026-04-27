package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentEvaluationMetric;
import com.se.EdgeHire.DTO.OfferAgentEvaluationRequest;
import com.se.EdgeHire.DTO.OfferAgentLlmJudgeRequest;
import com.se.EdgeHire.DTO.OfferAgentLlmJudgeResult;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LlmOfferAgentJudgeClient implements OfferAgentLlmJudgeClient {
    private static final Duration JUDGE_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient webClient;

    @Override
    public Optional<OfferAgentLlmJudgeResult> judge(
            OfferAgentEvaluationRequest request,
            List<OfferAgentEvaluationMetric> ruleMetrics,
            int ruleScore,
            String ruleGrade) {
        OfferAgentToolExecutionReport report = request.getToolReport() == null
                ? new OfferAgentToolExecutionReport()
                : request.getToolReport();
        OfferAgentLlmJudgeRequest judgeRequest = new OfferAgentLlmJudgeRequest(
                request.getConversationId(),
                request.getMessage(),
                request.getTargetPosition(),
                request.getFinalAnswer(),
                ruleScore,
                ruleGrade,
                ruleMetrics,
                report.getTrace() == null ? List.of() : report.getTrace(),
                report.getToolCalls() == null ? List.of() : report.getToolCalls()
        );

        try {
            OfferAgentLlmJudgeResult result = webClient.post()
                    .uri("/api/offer-agent/evaluation/judge")
                    .bodyValue(judgeRequest)
                    .retrieve()
                    .bodyToMono(OfferAgentLlmJudgeResult.class)
                    .block(JUDGE_TIMEOUT);
            if (result == null || !Boolean.TRUE.equals(result.getAvailable())) {
                return Optional.empty();
            }
            return Optional.of(result);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
