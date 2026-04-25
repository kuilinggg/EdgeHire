package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolParameter;
import com.se.EdgeHire.Entity.Resume;
import com.se.EdgeHire.Repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CalculateJobMatchScoreTool implements OfferAgentTool {
    private static final List<String> AI_AGENT_REQUIRED = List.of(
            "Java", "Spring Boot", "RAG", "Tool Calling", "AI Agent", "Prompt", "VectorStore", "Embedding"
    );

    private final ResumeRepository resumeRepository;

    @Override
    public String name() {
        return "calculate_job_match_score";
    }

    @Override
    public String description() {
        return "Calculate deterministic job match score from resume keywords and target role.";
    }

    @Override
    public List<OfferAgentToolParameter> parameters() {
        return List.of(
                new OfferAgentToolParameter("targetPosition", "string", "Target role or internship position to score against.", false)
        );
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        String resumeText = resumeRepository.findByUserId(context.getUserId()).stream()
                .map(Resume::getContent)
                .reduce("", (left, right) -> left + "\n" + right);
        String fullText = (resumeText + "\n" + context.getMessage()).toLowerCase(Locale.ROOT);
        List<String> matched = AI_AGENT_REQUIRED.stream()
                .filter(term -> fullText.contains(term.toLowerCase(Locale.ROOT)))
                .toList();
        List<String> missing = AI_AGENT_REQUIRED.stream()
                .filter(term -> !matched.contains(term))
                .toList();
        int score = Math.min(100, 35 + matched.size() * 8);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("targetRole", inferTargetRole(context));
        output.put("score", score);
        output.put("matchedKeywords", matched);
        output.put("missingKeywords", missing);
        output.put("summary", "job match score=" + score + ", matched=" + matched.size() + ", missing=" + missing.size());
        return output;
    }

    private String inferTargetRole(OfferAgentToolContext context) {
        Object targetPosition = context.getArguments().get("targetPosition");
        if (targetPosition != null && !String.valueOf(targetPosition).isBlank()) {
            return String.valueOf(targetPosition);
        }
        String message = context.getMessage();
        if (message != null && message.toLowerCase(Locale.ROOT).contains("agent")) {
            return "AI Agent 实习生";
        }
        return "用户问题中的目标岗位";
    }
}
