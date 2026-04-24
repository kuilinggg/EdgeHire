package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolPlan;
import com.se.EdgeHire.DTO.OfferAgentToolPlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LlmOfferAgentToolPlanner {
    private static final Duration TOOL_PLAN_TIMEOUT = Duration.ofSeconds(8);

    private final WebClient webClient;
    private final OfferAgentToolRegistry registry;

    public Optional<OfferAgentToolPlan> plan(String conversationId, String message, String userContext) {
        OfferAgentToolPlanRequest request = new OfferAgentToolPlanRequest(
                conversationId,
                message,
                userContext,
                registry.definitions()
        );

        try {
            OfferAgentToolPlan response = webClient.post()
                    .uri("/api/offer-agent/tool-plan")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OfferAgentToolPlan.class)
                    .block(TOOL_PLAN_TIMEOUT);
            return Optional.ofNullable(response);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
