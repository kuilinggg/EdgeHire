package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentMemoryResponse {
    private Integer userId;
    private String targetPosition;
    private String profileSummary;
    private List<String> skillTags = new ArrayList<>();
    private List<String> gapTags = new ArrayList<>();
    private List<String> preferenceTags = new ArrayList<>();
    private String suggestionSummary;
    private String evidenceSummary;
    private String source;
    private LocalDateTime lastInteractionAt;
}
