package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentMemoryResponse;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import com.se.EdgeHire.Entity.OfferAgentMemory;
import com.se.EdgeHire.Repository.OfferAgentMemoryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataRetrievalFailureException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OfferAgentMemoryServiceTest {

    @Test
    void updateFromInteractionBuildsLongTermProfileFromToolResultsAndAnswer() {
        OfferAgentMemoryRepository repository = mock(OfferAgentMemoryRepository.class);
        when(repository.findByUserId(7)).thenReturn(Optional.empty());
        OfferAgentMemoryService service = new OfferAgentMemoryService(repository);

        OfferAgentToolExecutionReport report = new OfferAgentToolExecutionReport(
                "rule_fallback",
                null,
                new ArrayList<>(),
                List.of(new OfferAgentToolResult(
                        "calculate_job_match_score",
                        "match",
                        "{}",
                        "{\"detectedSkills\":[\"Java\",\"RAG\"],\"missingKeywords\":[\"Agent Memory\",\"Evaluation\"]}",
                        "score=82",
                        true,
                        null
                ))
        );

        service.updateFromInteraction(
                7,
                "我想投 AI Agent 实习生",
                "建议继续强化 Java、Spring Boot、RAG 和 Tool Calling，同时补充 Agent Memory。",
                report
        );

        ArgumentCaptor<OfferAgentMemory> captor = ArgumentCaptor.forClass(OfferAgentMemory.class);
        verify(repository).save(captor.capture());
        OfferAgentMemory saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(7);
        assertThat(saved.getTargetPosition()).isEqualTo("AI Agent 实习生");
        assertThat(saved.getSkillTags()).contains("Java", "Spring Boot", "RAG", "Tool Calling");
        assertThat(saved.getGapTags()).contains("Agent Memory", "Evaluation");
        assertThat(saved.getProfileSummary()).contains("长期画像", "AI Agent 实习生");
        assertThat(saved.getSource()).isEqualTo("chat");
        assertThat(saved.getLastInteractionAt()).isNotNull();
    }

    @Test
    void findByUserIdParsesStoredJsonTags() {
        OfferAgentMemory memory = new OfferAgentMemory();
        memory.setUserId(7);
        memory.setTargetPosition("前端开发实习生");
        memory.setProfileSummary("长期画像：目标岗位=前端开发实习生");
        memory.setSkillTags("[\"Vue\",\"TypeScript\"]");
        memory.setGapTags("[\"React\"]");
        memory.setPreferenceTags("[\"前端开发实习生\"]");
        memory.setSuggestionSummary("继续完善项目表达");
        memory.setEvidenceSummary("最近记忆依据");
        memory.setSource("workflow");
        memory.setLastInteractionAt(LocalDateTime.of(2026, 4, 27, 10, 0));

        OfferAgentMemoryRepository repository = mock(OfferAgentMemoryRepository.class);
        when(repository.findByUserId(7)).thenReturn(Optional.of(memory));
        OfferAgentMemoryService service = new OfferAgentMemoryService(repository);

        OfferAgentMemoryResponse response = service.findByUserId(7);

        assertThat(response.getTargetPosition()).isEqualTo("前端开发实习生");
        assertThat(response.getSkillTags()).containsExactly("Vue", "TypeScript");
        assertThat(response.getGapTags()).containsExactly("React");
        assertThat(response.getPreferenceTags()).containsExactly("前端开发实习生");
        assertThat(response.getSource()).isEqualTo("workflow");
    }

    @Test
    void missingMemoryTableFallsBackToEmptyProfile() {
        OfferAgentMemoryRepository repository = mock(OfferAgentMemoryRepository.class);
        when(repository.findByUserId(any())).thenThrow(new DataRetrievalFailureException("table missing"));
        OfferAgentMemoryService service = new OfferAgentMemoryService(repository);

        OfferAgentMemoryResponse response = service.findByUserId(7);

        assertThat(response.getUserId()).isEqualTo(7);
        assertThat(response.getLastInteractionAt()).isNull();
    }
}
