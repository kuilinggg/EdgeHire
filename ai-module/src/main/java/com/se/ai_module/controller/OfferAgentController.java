package com.se.ai_module.controller;

import com.se.ai_module.dto.OfferAgentChatRequest;
import com.se.ai_module.service.OfferAgentPromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/offer-agent")
public class OfferAgentController {
    private static final Logger logger = LoggerFactory.getLogger(OfferAgentController.class);

    private final ChatClient offerAgentChatClient;
    private final OfferAgentPromptBuilder promptBuilder;

    public OfferAgentController(
            @Qualifier("offerAgentChatClient") ChatClient offerAgentChatClient,
            OfferAgentPromptBuilder promptBuilder) {
        this.offerAgentChatClient = offerAgentChatClient;
        this.promptBuilder = promptBuilder;
    }

    @PostMapping("/chat/stream")
    public Flux<String> chatStream(@RequestBody OfferAgentChatRequest request) {
        String requestConversationId = request.getConversationId();
        String conversationId = requestConversationId == null || requestConversationId.isBlank()
                ? "offer-agent-default"
                : requestConversationId;

        return offerAgentChatClient
                .prompt(promptBuilder.build(request))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .onErrorResume(e -> {
                    if (e instanceof TimeoutException) {
                        return Flux.just("Error: request timeout");
                    }
                    if (e instanceof WebClientResponseException.Unauthorized) {
                        return Flux.just("Error: invalid api key");
                    }
                    logger.warn("OfferAgent failed: {}", e.getMessage(), e);
                    return Flux.just("Error: offer agent service is unavailable");
                });
    }
}
