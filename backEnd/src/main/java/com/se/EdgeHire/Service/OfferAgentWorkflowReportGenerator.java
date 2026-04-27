package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentWorkflowStep;
import reactor.core.publisher.Flux;

import java.util.List;

public interface OfferAgentWorkflowReportGenerator {
    String generate(
            String conversationId,
            String workflowName,
            String targetPosition,
            List<OfferAgentWorkflowStep> steps,
            String fallbackReport
    );

    default Flux<String> generateStream(
            String conversationId,
            String workflowName,
            String targetPosition,
            List<OfferAgentWorkflowStep> steps,
            String fallbackReport
    ) {
        return Flux.just(generate(conversationId, workflowName, targetPosition, steps, fallbackReport));
    }
}
