package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.DTO.OfferAgentVectorSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OfferAgentVectorStoreServiceTest {

    @Test
    void vectorSearchFindsSemanticAgentDocument() {
        OfferAgentVectorStoreService vectorStoreService =
                new OfferAgentVectorStoreService(new LocalHashEmbeddingService());

        vectorStoreService.rebuild(List.of(
                document(1L, "AI Agent RAG Tool Calling", "LLM agent retrieval and tool workflow"),
                document(2L, "HR resume review", "Manual HR feedback and salary negotiation")
        ));

        List<OfferAgentVectorSearchResult> results = vectorStoreService.search(
                "大模型工具调用和检索增强项目",
                2
        );

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getChunkId()).isEqualTo(1L);
        assertThat(results.get(0).getSimilarity()).isGreaterThan(0);
    }

    private OfferAgentKnowledgeResult document(Long chunkId, String title, String content) {
        OfferAgentKnowledgeResult result = new OfferAgentKnowledgeResult();
        result.setChunkId(chunkId);
        result.setDocId(chunkId);
        result.setTitle(title);
        result.setTags(title);
        result.setTargetPosition(title);
        result.setSummary(content);
        result.setContent(content);
        return result;
    }
}
