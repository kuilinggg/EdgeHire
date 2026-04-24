package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentAiRequest;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.DTO.OfferAgentUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferAgentService {
    private final WebClient webClient;
    private final OfferAgentContextService contextService;
    private final OfferAgentToolExecutionService toolExecutionService;

    public Flux<String> chatStream(Integer userId, String conversationId, String message) {
        OfferAgentUserContext context = contextService.buildUserContext(userId, message);
        String plannerContext = contextService.toPromptContext(context);
        List<OfferAgentToolResult> toolResults = toolExecutionService.execute(userId, conversationId, message, plannerContext);
        context.setToolCalls(toolResults);
        String promptContext = contextService.toPromptContext(context);
        OfferAgentAiRequest request = new OfferAgentAiRequest(conversationId, message, promptContext);

        return webClient.post()
                .uri("/api/offer-agent/chat/stream")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class);
    }
}
