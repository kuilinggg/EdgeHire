package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Service.AiService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
public class AiController {
    private final AiService aiService;
    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @PostMapping(value = "/resumeOptimizeStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> resumeOptimizeStream(@RequestBody Map<String, String> request) {
        String conversationId = request.get("id");
        String resumeContent = request.get("resume");

        return aiService.resumeOptimizeStream(conversationId, resumeContent)
                .map(data -> "data: " + data + "\n\n")
                .doOnCancel(() -> logger.info("客户端断开连接"));
    }

    @PostMapping(value = "/resumeOptimize")
    public String resumeOptimize(@RequestBody Map<String, String> request) {
        String conversationId = request.get("id");
        String resumeContent = request.get("resume");

        return aiService.resumeOptimize(conversationId, resumeContent);
    }
}
