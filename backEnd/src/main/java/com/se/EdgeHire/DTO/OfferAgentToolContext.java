package com.se.EdgeHire.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class OfferAgentToolContext {
    private Integer userId;
    private String conversationId;
    private String message;
    private Map<String, Object> arguments = new LinkedHashMap<>();

    public OfferAgentToolContext(Integer userId, String conversationId, String message) {
        this(userId, conversationId, message, new LinkedHashMap<>());
    }

    public OfferAgentToolContext(Integer userId, String conversationId, String message, Map<String, Object> arguments) {
        this.userId = userId;
        this.conversationId = conversationId;
        this.message = message;
        this.arguments = arguments == null ? new LinkedHashMap<>() : arguments;
    }
}
