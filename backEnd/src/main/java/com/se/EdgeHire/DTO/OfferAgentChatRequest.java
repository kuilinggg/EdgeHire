package com.se.EdgeHire.DTO;

import lombok.Data;

@Data
public class OfferAgentChatRequest {
    private Integer userId;
    private String conversationId;
    private String message;
}
