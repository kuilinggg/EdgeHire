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
@Table(name = "offer_agent_memory")
@Data
public class OfferAgentMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "target_position", length = 120)
    private String targetPosition;

    @Column(name = "profile_summary", columnDefinition = "TEXT")
    private String profileSummary;

    @Column(name = "skill_tags", columnDefinition = "TEXT")
    private String skillTags;

    @Column(name = "gap_tags", columnDefinition = "TEXT")
    private String gapTags;

    @Column(name = "preference_tags", columnDefinition = "TEXT")
    private String preferenceTags;

    @Column(name = "suggestion_summary", columnDefinition = "TEXT")
    private String suggestionSummary;

    @Column(name = "evidence_summary", columnDefinition = "TEXT")
    private String evidenceSummary;

    @Column(name = "source", length = 40)
    private String source;

    @Column(name = "last_interaction_at")
    private LocalDateTime lastInteractionAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
