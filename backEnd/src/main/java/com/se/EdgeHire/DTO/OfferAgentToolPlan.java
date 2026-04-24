package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolPlan {
    private String source;
    private String fallbackReason;
    private List<OfferAgentPlannedToolCall> toolCalls = new ArrayList<>();
}
