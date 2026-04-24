package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentKnowledgeSearchResponse {
    private String query;
    private String retrievalMode;
    private Integer total;
    private List<OfferAgentKnowledgeResult> results;
}
