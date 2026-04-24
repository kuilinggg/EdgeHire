package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolResult {
    private String toolName;
    private String description;
    private String inputJson;
    private String outputJson;
    private String summary;
    private Boolean success;
    private String errorMessage;
}
