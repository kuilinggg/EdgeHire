package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolDefinition;

import java.util.List;
import java.util.Map;

public interface OfferAgentTool {
    String name();

    String description();

    default List<com.se.EdgeHire.DTO.OfferAgentToolParameter> parameters() {
        return List.of();
    }

    default String returnDescription() {
        return "Structured JSON output with a short summary field.";
    }

    default boolean displayable() {
        return true;
    }

    default boolean requiresUserContext() {
        return true;
    }

    default OfferAgentToolDefinition definition() {
        return new OfferAgentToolDefinition(
                name(),
                description(),
                parameters(),
                returnDescription(),
                displayable(),
                requiresUserContext()
        );
    }

    Map<String, Object> execute(OfferAgentToolContext context);
}
