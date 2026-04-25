package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentWorkflowRequest {
    private Integer userId;
    private String conversationId;
    private String targetPosition;
}
