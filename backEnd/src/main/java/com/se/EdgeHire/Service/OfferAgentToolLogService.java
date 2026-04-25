package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentToolCallLogResponse;
import com.se.EdgeHire.Entity.OfferAgentToolCallLog;
import com.se.EdgeHire.Repository.OfferAgentToolCallLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferAgentToolLogService {
    private final OfferAgentToolCallLogRepository logRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<OfferAgentToolCallLogResponse> findByConversationId(String conversationId) {
        return logRepository.findTop50ByConversationIdOrderByCreatedAtDesc(conversationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private OfferAgentToolCallLogResponse toResponse(OfferAgentToolCallLog log) {
        return new OfferAgentToolCallLogResponse(
                log.getId(),
                log.getUserId(),
                log.getConversationId(),
                log.getToolName(),
                extractSummary(log.getOutputJson()),
                log.getSuccess(),
                log.getErrorMessage(),
                log.getPlanSource(),
                log.getCreatedAt()
        );
    }

    private String extractSummary(String outputJson) {
        if (outputJson == null || outputJson.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(outputJson);
            return root.path("summary").asText("");
        } catch (Exception ignored) {
            return "";
        }
    }
}
