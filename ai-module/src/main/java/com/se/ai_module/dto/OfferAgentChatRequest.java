package com.se.ai_module.dto;

import lombok.Data;

@Data
public class OfferAgentChatRequest {
    private String conversationId;
    private String message;
    private String userContext;
}
