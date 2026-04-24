package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.Repository.OfferAgentKnowledgeChunkRepository;
import com.se.EdgeHire.Repository.OfferAgentRetrievalLogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferAgentKnowledgeServiceTest {

    @Test
    void retrieveRanksBuiltinAgentKnowledgeForAgentQuery() {
        OfferAgentKnowledgeChunkRepository chunkRepository = mock(OfferAgentKnowledgeChunkRepository.class);
        OfferAgentRetrievalLogRepository logRepository = mock(OfferAgentRetrievalLogRepository.class);
        when(chunkRepository.findAllWithDocument()).thenReturn(List.of());
        OfferAgentVectorStoreService vectorStoreService = new OfferAgentVectorStoreService(new LocalHashEmbeddingService());

        OfferAgentKnowledgeService service = new OfferAgentKnowledgeService(chunkRepository, logRepository, vectorStoreService);

        List<OfferAgentKnowledgeResult> results = service.retrieve(
                7,
                "武汉大学 软件工程 AI Agent 实习 RAG Tool Calling 简历"
        );

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("AI Agent");
        assertThat(results.stream()
                .map(OfferAgentKnowledgeResult::getTitle)
                .anyMatch(title -> title.contains("RAG"))).isTrue();
        assertThat(results.get(0).getScore()).isGreaterThan(0);
        assertThat(results.get(0).getRetrievalMode()).isIn("hybrid", "vector", "keyword");
        assertThat(results.stream().anyMatch(result -> result.getVectorScore() != null && result.getVectorScore() > 0))
                .isTrue();
    }
}
