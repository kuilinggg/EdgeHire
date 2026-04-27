package com.se.EdgeHire.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OfferAgentUserContext {
    private Integer userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String education;
    private String school;
    private Integer membership;
    private List<String> favorPositions = new ArrayList<>();
    private List<ResumeSnapshot> resumes = new ArrayList<>();
    private List<PostSnapshot> posts = new ArrayList<>();
    private List<GuidanceSnapshot> guidanceRequests = new ArrayList<>();
    private List<KnowledgeSnapshot> retrievedKnowledge = new ArrayList<>();
    private List<OfferAgentToolResult> toolCalls = new ArrayList<>();
    private List<String> toolTrace = new ArrayList<>();
    private MemorySnapshot memory;

    @Data
    public static class ResumeSnapshot {
        private Integer id;
        private LocalDateTime createTime;
        private String contentPreview;
    }

    @Data
    public static class PostSnapshot {
        private Integer id;
        private Integer resumeId;
        private String school;
        private String education;
        private List<String> favorPositions = new ArrayList<>();
    }

    @Data
    public static class GuidanceSnapshot {
        private Long id;
        private String targetPosition;
        private String guidanceType;
        private String status;
        private String feedbackPreview;
        private LocalDateTime requestTime;
    }

    @Data
    public static class KnowledgeSnapshot {
        private Long chunkId;
        private Long docId;
        private String title;
        private String category;
        private String tags;
        private String targetPosition;
        private String source;
        private String summary;
        private String contentPreview;
        private Integer score;
        private Integer keywordScore;
        private Double vectorScore;
        private Double hybridScore;
        private String retrievalMode;
    }

    @Data
    public static class MemorySnapshot {
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
}
