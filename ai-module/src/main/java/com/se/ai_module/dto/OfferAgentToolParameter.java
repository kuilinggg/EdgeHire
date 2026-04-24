package com.se.ai_module.dto;

import lombok.Data;

@Data
public class OfferAgentToolParameter {
    private String name;
    private String type;
    private String description;
    private Boolean required;
}
