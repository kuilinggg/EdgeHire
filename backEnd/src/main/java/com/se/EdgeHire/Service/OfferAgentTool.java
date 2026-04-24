package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;

import java.util.Map;

public interface OfferAgentTool {
    String name();

    String description();

    Map<String, Object> execute(OfferAgentToolContext context);
}
