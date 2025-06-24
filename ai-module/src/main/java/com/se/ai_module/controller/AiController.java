package com.se.ai_module.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final ChatClient  chatClient;

    @RequestMapping("/test")
    public String test(@RequestParam(defaultValue = "讲个笑话") String prompt, HttpSession session) {
        return chatClient
                .prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getId()))
                .call()
                .content();
    }

}
