package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentAiRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class OfferAgentService {
    private final WebClient webClient;
    private final OfferAgentContextService contextService;

    public Flux<String> chatStream(Integer userId, String conversationId, String message) {
        String userContext = contextService.buildPromptContext(userId, message);
        OfferAgentAiRequest request = new OfferAgentAiRequest(conversationId, message, userContext);

        return webClient.post()
                .uri("/api/offer-agent/chat/stream")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class);
    }
}
