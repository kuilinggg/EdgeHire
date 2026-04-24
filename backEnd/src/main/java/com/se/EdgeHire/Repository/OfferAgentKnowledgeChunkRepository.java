package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.OfferAgentKnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferAgentKnowledgeChunkRepository extends JpaRepository<OfferAgentKnowledgeChunk, Long> {
    @Query("SELECT c FROM OfferAgentKnowledgeChunk c LEFT JOIN FETCH c.document")
    List<OfferAgentKnowledgeChunk> findAllWithDocument();
}
