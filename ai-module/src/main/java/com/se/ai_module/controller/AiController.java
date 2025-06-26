package com.se.ai_module.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final ChatClient chatClient;

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @RequestMapping("/test")
    public String test(@RequestParam(defaultValue = "讲个笑话") String prompt, HttpSession session) {
        return chatClient
                .prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getId()))
                .call()
                .content();
    }

    @RequestMapping(value = "/testFlux", produces = "text/html;charset=UTF-8")
    public Flux<String> testFlux(@RequestParam(defaultValue = "讲个笑话") String prompt, HttpSession session) {
        return chatClient
                .prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getId()))
                .stream()
                .content().onErrorResume(e -> {
                    if(e instanceof TimeoutException) {
                        return Flux.just("Error: 请求超时");
                    }
                    else if(e instanceof WebClientResponseException.Unauthorized) {
                        return Flux.just("Error: Api-Key错误");
                    }
                    else {
                        return Flux.just("Error: 当前服务不可用");
                    }
                });
    }

    @PostMapping(value = "/optimize")
    public String optimize(@RequestBody Map<String, String> request) {
        String conversationId = request.get("id");
        String resumeContent = request.get("resume");
        return chatClient
                .prompt(resumeContent)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    @PostMapping(value = "/optimizeStream")
    public Flux<String> optimizeStream(@RequestBody Map<String, String> request) {
        String conversationId = request.get("id");
        String resumeContent = request.get("resume");
        return chatClient
                .prompt(resumeContent)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .onErrorResume(e -> {
                    if(e instanceof TimeoutException) {
                        return Flux.just("Error: 请求超时");
                    }
                    else if(e instanceof WebClientResponseException.Unauthorized) {
                        return Flux.just("Error: Api-Key错误");
                    }
                    else {
                        logger.warn("出现未知错误： {}", e.getMessage(), e);
                        return Flux.just("Error: 当前服务不可用");
                    }
                });
    }
}
