package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.DTO.OfferAgentVectorSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OfferAgentVectorStoreService {
    private final OfferAgentEmbeddingService embeddingService;
    private final Map<Long, VectorEntry> entries = new ConcurrentHashMap<>();

    public synchronized int rebuild(List<OfferAgentKnowledgeResult> documents) {
        entries.clear();
        for (OfferAgentKnowledgeResult document : documents) {
            if (document.getChunkId() == null || document.getContent() == null || document.getContent().isBlank()) {
                continue;
            }
            String text = document.getTitle() + "\n"
                    + document.getTags() + "\n"
                    + document.getTargetPosition() + "\n"
                    + document.getSummary() + "\n"
                    + document.getContent();
            entries.put(document.getChunkId(), new VectorEntry(document.getChunkId(), embeddingService.embed(text)));
        }
        return entries.size();
    }

    public List<OfferAgentVectorSearchResult> search(String query, int topK) {
        if (entries.isEmpty() || query == null || query.isBlank()) {
            return List.of();
        }
        double[] queryVector = embeddingService.embed(query);
        return entries.values().stream()
                .map(entry -> new OfferAgentVectorSearchResult(entry.chunkId(), cosine(queryVector, entry.vector())))
                .filter(result -> result.getSimilarity() > 0)
                .sorted(Comparator.comparing(OfferAgentVectorSearchResult::getSimilarity).reversed())
                .limit(topK)
                .toList();
    }

    public int size() {
        return entries.size();
    }

    private double cosine(double[] left, double[] right) {
        double sum = 0;
        int length = Math.min(left.length, right.length);
        for (int i = 0; i < length; i++) {
            sum += left[i] * right[i];
        }
        return sum;
    }

    private record VectorEntry(Long chunkId, double[] vector) {
    }
}
