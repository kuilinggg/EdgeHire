package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RetrieveKnowledgeTool implements OfferAgentTool {
    private final OfferAgentKnowledgeService knowledgeService;

    @Override
    public String name() {
        return "retrieve_knowledge";
    }

    @Override
    public String description() {
        return "Run hybrid RAG retrieval against OfferAgent knowledge chunks.";
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        List<OfferAgentKnowledgeResult> results = knowledgeService.retrieve(context.getUserId(), context.getMessage());
        List<Map<String, Object>> sources = results.stream()
                .map(result -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("title", result.getTitle());
                    item.put("category", result.getCategory());
                    item.put("score", result.getScore());
                    item.put("retrievalMode", result.getRetrievalMode());
                    item.put("summary", result.getSummary());
                    return item;
                })
                .toList();
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("hitCount", results.size());
        output.put("sources", sources);
        output.put("summary", "hybrid RAG retrieved " + results.size() + " knowledge chunk(s)");
        return output;
    }
}
