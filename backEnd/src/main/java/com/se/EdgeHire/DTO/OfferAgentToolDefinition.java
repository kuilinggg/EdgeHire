package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolDefinition {
    private String name;
    private String description;
    private List<OfferAgentToolParameter> parameters = new ArrayList<>();
    private String returnDescription;
    private Boolean displayable;
    private Boolean requiresUserContext;
}
