package com.se.ai_module.dto;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class OfferAgentPlannedToolCall {
    private String toolName;
    private Map<String, Object> arguments = new LinkedHashMap<>();
}
