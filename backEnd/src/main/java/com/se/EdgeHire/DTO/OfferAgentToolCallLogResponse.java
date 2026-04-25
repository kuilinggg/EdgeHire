package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolCallLogResponse {
    private Long id;
    private Integer userId;
    private String conversationId;
    private String toolName;
    private String summary;
    private Boolean success;
    private String errorMessage;
    private String planSource;
    private LocalDateTime createdAt;
}
