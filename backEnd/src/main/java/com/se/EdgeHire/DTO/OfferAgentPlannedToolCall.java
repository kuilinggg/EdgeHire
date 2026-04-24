package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentPlannedToolCall {
    private String toolName;
    private Map<String, Object> arguments = new LinkedHashMap<>();
}
