package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentKnowledgeResult {
    private Long chunkId;
    private Long docId;
    private String title;
    private String category;
    private String tags;
    private String targetPosition;
    private String source;
    private String summary;
    private String content;
    private Integer score;
    private Integer keywordScore;
    private Double vectorScore;
    private Double hybridScore;
    private String retrievalMode;
}
