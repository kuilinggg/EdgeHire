package com.se.EdgeHire.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferAgentEvaluationMetric {
    private String name;
    private Integer score;
    private String status;
    private String summary;
    private String evidence;
}
