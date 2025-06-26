package com.se.EdgeHire.Service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@AllArgsConstructor
@Service
public class AiService {
    private final WebClient webClient;

    public Flux<String> resumeOptimizeStream(String conversationId, String resumeContent) {
        return webClient.post()
                .uri("/api/ai/optimizeStream")
                .bodyValue(Map.of("id", conversationId, "resume", resumeContent))
                .retrieve()
                .bodyToFlux(String.class);
    }

    public String resumeOptimize(String conversationId, String resumeContent) {
        return webClient.post()
                .uri("/api/ai/optimize")
                .bodyValue(Map.of("id", conversationId, "resume", resumeContent))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
