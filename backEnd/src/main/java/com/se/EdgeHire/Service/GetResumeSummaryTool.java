package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
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
public class GetResumeSummaryTool implements OfferAgentTool {
    private static final List<String> SKILL_TERMS = List.of(
            "Java", "Spring Boot", "MySQL", "Redis", "RAG", "Tool Calling",
            "AI Agent", "VectorStore", "Embedding", "Vue", "JPA", "WebSocket"
    );

    private final ResumeRepository resumeRepository;

    @Override
    public String name() {
        return "get_resume_summary";
    }

    @Override
    public String description() {
        return "Summarize resumes and extract deterministic skill keywords.";
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        List<Resume> resumes = resumeRepository.findByUserId(context.getUserId());
        String combined = resumes.stream().map(Resume::getContent).reduce("", (left, right) -> left + "\n" + right);
        List<String> detectedSkills = SKILL_TERMS.stream()
                .filter(term -> combined.toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT)))
                .toList();

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("resumeCount", resumes.size());
        output.put("latestResumeId", resumes.isEmpty() ? null : resumes.get(resumes.size() - 1).getId());
        output.put("detectedSkills", detectedSkills);
        output.put("contentPreview", combined.length() > 1200 ? combined.substring(0, 1200) + "...[truncated]" : combined);
        output.put("summary", "loaded " + resumes.size() + " resume(s), detected " + detectedSkills.size() + " skill keyword(s)");
        return output;
    }
}
