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
    private final ResumeRepository resumeRepository;

    @Override
    public String name() {
        return "calculate_job_match_score";
    }

    @Override
    public String description() {
        return "\u6839\u636e\u7b80\u5386\u5173\u952e\u8bcd\u548c\u76ee\u6807\u5c97\u4f4d\u753b\u50cf\u8ba1\u7b97\u786e\u5b9a\u6027\u5339\u914d\u5ea6\u3002";
    }

    @Override
    public List<OfferAgentToolParameter> parameters() {
        return List.of(
                new OfferAgentToolParameter("targetPosition", "string", "\u76ee\u6807\u5c97\u4f4d\u6216\u5b9e\u4e60\u65b9\u5411\u3002", false)
        );
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        String targetRole = inferTargetRole(context);
        RoleKeywordProfile profile = roleKeywordProfile(targetRole);
        String resumeText = resumeRepository.findByUserId(context.getUserId()).stream()
                .map(Resume::getContent)
                .reduce("", (left, right) -> left + "\n" + right);
        String normalizedResume = resumeText.toLowerCase(Locale.ROOT);

        List<String> matched = profile.requiredKeywords().stream()
                .filter(term -> normalizedResume.contains(term.toLowerCase(Locale.ROOT)))
                .toList();
        List<String> missing = profile.requiredKeywords().stream()
                .filter(term -> !matched.contains(term))
                .toList();
        int score = Math.min(100, 35 + matched.size() * 8);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("targetRole", targetRole);
        output.put("roleCategory", profile.category());
        output.put("score", score);
        output.put("requiredKeywords", profile.requiredKeywords());
        output.put("matchedKeywords", matched);
        output.put("missingKeywords", missing);
        output.put("summary", "\u5c97\u4f4d\u5339\u914d\u5ea6=" + score
                + "\uff0c\u76ee\u6807\u65b9\u5411=" + profile.category()
                + "\uff0c\u5df2\u547d\u4e2d=" + matched.size()
                + "\uff0c\u5f85\u8865\u5f3a=" + missing.size());
        return output;
    }

    private String inferTargetRole(OfferAgentToolContext context) {
        Object targetPosition = context.getArguments().get("targetPosition");
        if (targetPosition != null && !String.valueOf(targetPosition).isBlank()) {
            return String.valueOf(targetPosition).trim();
        }
        String message = context.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return "AI Agent \u5b9e\u4e60\u751f";
    }

    private RoleKeywordProfile roleKeywordProfile(String targetRole) {
        String text = targetRole.toLowerCase(Locale.ROOT);
        if (containsAny(text, "\u524d\u7aef", "frontend", "vue", "react")) {
            return new RoleKeywordProfile(
                    "\u524d\u7aef\u5f00\u53d1",
                    List.of("Vue", "React", "TypeScript", "JavaScript", "CSS", "Vite", "Element Plus", "\u63a5\u53e3\u8054\u8c03")
            );
        }
        if (containsAny(text, "\u540e\u7aef", "backend", "java", "spring")) {
            return new RoleKeywordProfile(
                    "Java \u540e\u7aef\u5f00\u53d1",
                    List.of("Java", "Spring Boot", "JPA", "MySQL", "Redis", "REST API", "WebClient", "JUnit")
            );
        }
        if (containsAny(text, "\u6570\u636e", "\u7b97\u6cd5", "\u673a\u5668\u5b66\u4e60", "ml", "python", "\u5206\u6790")) {
            return new RoleKeywordProfile(
                    "\u6570\u636e/\u7b97\u6cd5",
                    List.of("Python", "SQL", "\u6570\u636e\u6e05\u6d17", "\u7279\u5f81\u5de5\u7a0b", "\u6a21\u578b\u8bc4\u4f30", "Embedding", "\u53ec\u56de\u7387", "A/B \u5206\u6790")
            );
        }
        if (containsAny(text, "\u4ea7\u54c1", "pm", "\u8fd0\u8425")) {
            return new RoleKeywordProfile(
                    "\u4ea7\u54c1/\u8fd0\u8425",
                    List.of("\u7528\u6237\u9700\u6c42", "\u4e1a\u52a1\u6d41\u7a0b", "\u539f\u578b\u8bbe\u8ba1", "\u6570\u636e\u6307\u6807", "\u8f6c\u5316\u7387", "\u7528\u6237\u53cd\u9988", "\u9700\u6c42\u4f18\u5148\u7ea7", "\u7ade\u54c1\u5206\u6790")
            );
        }
        return new RoleKeywordProfile(
                "AI Agent \u5e94\u7528\u5f00\u53d1",
                List.of("Java", "Spring Boot", "RAG", "Tool Calling", "AI Agent", "Prompt", "VectorStore", "Embedding")
        );
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private record RoleKeywordProfile(String category, List<String> requiredKeywords) {
    }
}
