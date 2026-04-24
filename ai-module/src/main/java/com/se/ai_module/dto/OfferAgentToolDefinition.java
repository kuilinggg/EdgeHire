package com.se.ai_module.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OfferAgentToolDefinition {
    private String name;
    private String description;
    private List<OfferAgentToolParameter> parameters = new ArrayList<>();
    private String returnDescription;
    private Boolean displayable;
    private Boolean requiresUserContext;
}
