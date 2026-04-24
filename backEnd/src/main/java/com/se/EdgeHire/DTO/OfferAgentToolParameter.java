package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentToolParameter {
    private String name;
    private String type;
    private String description;
    private Boolean required;
}
