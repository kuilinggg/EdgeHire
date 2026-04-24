package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentUserContext;
import com.se.EdgeHire.Entity.GuidanceRequest;
import com.se.EdgeHire.Entity.Info;
import com.se.EdgeHire.Entity.Post;
import com.se.EdgeHire.Entity.Resume;
import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.GuidanceRequestRepository;
import com.se.EdgeHire.Repository.InfoRepository;
import com.se.EdgeHire.Repository.PostRepository;
import com.se.EdgeHire.Repository.ResumeRepository;
import com.se.EdgeHire.Repository.SeekerInfoRepository;
import com.se.EdgeHire.Repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferAgentContextServiceTest {

    @Test
    void buildUserContextAggregatesExistingCareerData() {
        UserRepository userRepository = mock(UserRepository.class);
        InfoRepository infoRepository = mock(InfoRepository.class);
        SeekerInfoRepository seekerInfoRepository = mock(SeekerInfoRepository.class);
        ResumeRepository resumeRepository = mock(ResumeRepository.class);
        PostRepository postRepository = mock(PostRepository.class);
        GuidanceRequestRepository guidanceRequestRepository = mock(GuidanceRequestRepository.class);
        OfferAgentKnowledgeService knowledgeService = mock(OfferAgentKnowledgeService.class);

        User user = new User();
        user.setId(7);
        user.setUsername("whu-student");
        user.setRole(1);

        Info info = new Info();
        info.setUserId(7);
        info.setRealname("Alex");
        info.setEmail("alex@example.com");

        SeekerInfo seekerInfo = new SeekerInfo();
        seekerInfo.setUserId(7);
        seekerInfo.setEducation(5);
        seekerInfo.setSchool("Wuhan University");
        seekerInfo.setFavor("[\"AI Agent Intern\",\"Java Backend Intern\"]");
        seekerInfo.setMembership(1);

        Resume resume = new Resume();
        resume.setId(11);
        resume.setUserId(7);
        resume.setCreateTime(LocalDateTime.of(2026, 4, 1, 10, 0));
        resume.setContent("Java, Spring Boot, RAG, Tool Calling project");

        Post post = new Post();
        post.setId(21);
        post.setUserId(7);
        post.setResumeId(11);
        post.setSeekerInfo(seekerInfo);

        GuidanceRequest guidanceRequest = new GuidanceRequest();
        guidanceRequest.setId(31L);
        guidanceRequest.setTargetPosition("AI Agent Intern");
        guidanceRequest.setGuidanceType("resume");
        guidanceRequest.setStatus("completed");
        guidanceRequest.setFeedback("Strengthen the agent workflow section.");

        when(userRepository.findById(7)).thenReturn(Optional.of(user));
        when(infoRepository.findByUserId(7)).thenReturn(List.of(info));
        when(seekerInfoRepository.findByUserId(7)).thenReturn(Optional.of(seekerInfo));
        when(resumeRepository.findByUserId(7)).thenReturn(List.of(resume));
        when(postRepository.findByUserIdWithDetails(7)).thenReturn(List.of(post));
        when(guidanceRequestRepository.findByUserIdAndDeletedAtIsNullOrderByRequestTimeDesc(7L))
                .thenReturn(List.of(guidanceRequest));
        when(knowledgeService.retrieve(eq(7), contains("AI Agent")))
                .thenReturn(List.of(new com.se.EdgeHire.DTO.OfferAgentKnowledgeResult(
                        -1L,
                        -1L,
                        "AI Agent 实习岗位能力模型",
                        "岗位能力模型",
                        "AI Agent,RAG,Tool Calling",
                        "AI Agent 实习生",
                        "builtin",
                        "AI Agent internships value RAG and tool calling.",
                        "RAG and Tool Calling are important for AI Agent internships.",
                        25
                )));

        OfferAgentContextService service = new OfferAgentContextService(
                userRepository,
                infoRepository,
                seekerInfoRepository,
                resumeRepository,
                postRepository,
                guidanceRequestRepository,
                knowledgeService
        );

        OfferAgentUserContext context = service.buildUserContext(7, "I want an AI Agent internship plan.");
        String promptContext = service.toPromptContext(context);

        assertThat(context.getUsername()).isEqualTo("whu-student");
        assertThat(context.getRealName()).isEqualTo("Alex");
        assertThat(context.getEducation()).isEqualTo("bachelor");
        assertThat(context.getFavorPositions()).containsExactly("AI Agent Intern", "Java Backend Intern");
        assertThat(context.getResumes()).hasSize(1);
        assertThat(context.getPosts()).hasSize(1);
        assertThat(context.getGuidanceRequests()).hasSize(1);
        assertThat(context.getRetrievedKnowledge()).hasSize(1);
        assertThat(context.getToolTrace()).anyMatch(trace -> trace.contains("getResumeTool"));
        assertThat(promptContext).contains("Tool Calling Results", "Retrieved Knowledge", "AI Agent Intern", "Java, Spring Boot");
    }
}
