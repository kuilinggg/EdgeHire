package com.se.EdgeHire.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer_agent_retrieval_log")
@Data
public class OfferAgentRetrievalLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String queryText;

    @Column(name = "matched_chunk_ids", length = 500)
    private String matchedChunkIds;

    @Column(name = "top_score")
    private Integer topScore;

    @Column(name = "retrieval_mode", length = 50)
    private String retrievalMode = "hybrid";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
