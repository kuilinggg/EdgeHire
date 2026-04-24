package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolParameter;
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
    public List<OfferAgentToolParameter> parameters() {
        return List.of(
                new OfferAgentToolParameter("query", "string", "Search query for job-search knowledge retrieval.", false),
                new OfferAgentToolParameter("topK", "integer", "Maximum number of knowledge chunks to return.", false)
        );
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        String query = String.valueOf(context.getArguments().getOrDefault("query", context.getMessage()));
        Integer topK = parseTopK(context.getArguments().get("topK"));
        List<OfferAgentKnowledgeResult> results = topK == null
                ? knowledgeService.retrieve(context.getUserId(), query)
                : knowledgeService.retrieve(query, topK);
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

    private Integer parseTopK(Object value) {
        if (value == null) return null;
        try {
            int parsed = Integer.parseInt(String.valueOf(value));
            return Math.max(1, Math.min(8, parsed));
        } catch (Exception ignored) {
            return null;
        }
    }
}
