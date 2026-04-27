package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.Entity.Resume;
import com.se.EdgeHire.Repository.ResumeRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferAgentRoleAwareToolTest {

    @Test
    @SuppressWarnings("unchecked")
    void calculateJobMatchScoreUsesTargetSpecificKeywordProfile() {
        ResumeRepository resumeRepository = mock(ResumeRepository.class);
        Resume resume = new Resume();
        resume.setContent("Vue TypeScript Vite Element Plus CSS \u63a5\u53e3\u8054\u8c03");
        when(resumeRepository.findByUserId(7)).thenReturn(List.of(resume));

        CalculateJobMatchScoreTool tool = new CalculateJobMatchScoreTool(resumeRepository);

        Map<String, Object> frontend = tool.execute(new OfferAgentToolContext(
                7,
                "c1",
                "",
                Map.of("targetPosition", "\u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f")
        ));
        Map<String, Object> backend = tool.execute(new OfferAgentToolContext(
                7,
                "c2",
                "",
                Map.of("targetPosition", "Java \u540e\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f")
        ));

        assertThat(frontend.get("roleCategory")).isEqualTo("\u524d\u7aef\u5f00\u53d1");
        assertThat((List<String>) frontend.get("matchedKeywords")).contains("Vue", "TypeScript", "Vite");
        assertThat((List<String>) frontend.get("missingKeywords")).doesNotContain("Vue", "TypeScript", "Vite");
        assertThat(backend.get("roleCategory")).isEqualTo("Java \u540e\u7aef\u5f00\u53d1");
        assertThat((List<String>) backend.get("requiredKeywords")).contains("Spring Boot", "JPA", "MySQL");
        assertThat((List<String>) backend.get("missingKeywords")).contains("Spring Boot", "JPA", "MySQL");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateInterviewPlanUsesTargetSpecificQuestions() {
        GenerateInterviewPlanTool tool = new GenerateInterviewPlanTool();

        Map<String, Object> frontend = tool.execute(new OfferAgentToolContext(
                7,
                "c1",
                "",
                Map.of("targetPosition", "\u524d\u7aef\u5f00\u53d1\u5b9e\u4e60\u751f")
        ));
        Map<String, Object> data = tool.execute(new OfferAgentToolContext(
                7,
                "c2",
                "",
                Map.of("targetPosition", "\u6570\u636e\u5206\u6790\u5b9e\u4e60\u751f")
        ));

        assertThat(frontend.get("roleCategory")).isEqualTo("\u524d\u7aef\u5f00\u53d1");
        assertThat((List<String>) frontend.get("questions"))
                .anySatisfy(question -> assertThat(question).contains("Vue"))
                .anySatisfy(question -> assertThat(question).contains("\u63a5\u53e3\u9519\u8bef"));
        assertThat(data.get("roleCategory")).isEqualTo("\u6570\u636e/\u7b97\u6cd5");
        assertThat((List<String>) data.get("questions"))
                .anySatisfy(question -> assertThat(question).contains("Embedding"))
                .anySatisfy(question -> assertThat(question).contains("A/B"));
    }
}
