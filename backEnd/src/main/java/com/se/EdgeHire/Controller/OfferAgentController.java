package com.se.EdgeHire.Controller;

import com.se.EdgeHire.DTO.OfferAgentChatRequest;
import com.se.EdgeHire.DTO.OfferAgentKnowledgeSearchResponse;
import com.se.EdgeHire.DTO.OfferAgentUserContext;
import com.se.EdgeHire.Service.OfferAgentContextService;
import com.se.EdgeHire.Service.OfferAgentKnowledgeService;
import com.se.EdgeHire.Service.OfferAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/offer-agent")
@RequiredArgsConstructor
public class OfferAgentController {
    private final OfferAgentService offerAgentService;
    private final OfferAgentContextService contextService;
    private final OfferAgentKnowledgeService knowledgeService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody OfferAgentChatRequest request) {
        if (request.getUserId() == null) {
            return Flux.just("Error: userId is required");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return Flux.just("Error: message is required");
        }

        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = "offer-agent-" + request.getUserId();
        }

        return offerAgentService.chatStream(request.getUserId(), conversationId, request.getMessage());
    }

    @GetMapping("/context/{userId}")
    public ResponseEntity<OfferAgentUserContext> getContext(@PathVariable Integer userId) {
        return ResponseEntity.ok(contextService.buildUserContext(userId));
    }

    @GetMapping("/knowledge/search")
    public ResponseEntity<OfferAgentKnowledgeSearchResponse> searchKnowledge(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") Integer topK) {
        var results = knowledgeService.retrieve(query, topK);
        return ResponseEntity.ok(new OfferAgentKnowledgeSearchResponse(
                query,
                "hybrid",
                results.size(),
                results
        ));
    }

    @PostMapping("/knowledge/rebuild-index")
    public ResponseEntity<OfferAgentKnowledgeSearchResponse> rebuildKnowledgeIndex() {
        int indexed = knowledgeService.rebuildVectorIndex();
        return ResponseEntity.ok(new OfferAgentKnowledgeSearchResponse(
                "rebuild-index",
                "vector",
                indexed,
                java.util.List.of()
        ));
    }
}
