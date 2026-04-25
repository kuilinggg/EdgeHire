package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolExecutionReport {
    private String planSource;
    private String fallbackReason;
    private List<String> trace = new ArrayList<>();
    private List<OfferAgentToolResult> toolCalls = new ArrayList<>();
}
