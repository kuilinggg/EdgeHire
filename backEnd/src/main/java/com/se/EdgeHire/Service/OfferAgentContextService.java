package com.se.EdgeHire.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferAgentContextService {
    private static final int RESUME_PREVIEW_LIMIT = 1800;
    private static final int FEEDBACK_PREVIEW_LIMIT = 500;

    private final UserRepository userRepository;
    private final InfoRepository infoRepository;
    private final SeekerInfoRepository seekerInfoRepository;
    private final ResumeRepository resumeRepository;
    private final PostRepository postRepository;
    private final GuidanceRequestRepository guidanceRequestRepository;
    private final OfferAgentKnowledgeService knowledgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentUserContext buildUserContext(Integer userId) {
        return buildUserContext(userId, null);
    }

    public OfferAgentUserContext buildUserContext(Integer userId, String message) {
        OfferAgentUserContext context = new OfferAgentUserContext();
        context.setUserId(userId);

        loadUserProfile(context, userId);
        loadSeekerProfile(context, userId);
        loadResumes(context, userId);
        loadPostHistory(context, userId);
        loadGuidanceHistory(context, userId);
        loadRetrievedKnowledge(context, userId, message);

        return context;
    }

    public String buildPromptContext(Integer userId) {
        return buildPromptContext(userId, null);
    }

    public String buildPromptContext(Integer userId, String message) {
        return toPromptContext(buildUserContext(userId, message));
    }

    public String toPromptContext(OfferAgentUserContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("## Tool Calling Results\n");
        for (String trace : context.getToolTrace()) {
            builder.append("- ").append(trace).append('\n');
        }

        builder.append("\n## User Profile\n");
        appendLine(builder, "userId", context.getUserId());
        appendLine(builder, "username", context.getUsername());
        appendLine(builder, "realName", context.getRealName());
        appendLine(builder, "phone", context.getPhone());
        appendLine(builder, "email", context.getEmail());
        appendLine(builder, "education", context.getEducation());
        appendLine(builder, "school", context.getSchool());
        appendLine(builder, "membership", membershipText(context.getMembership()));
        appendLine(builder, "targetPositions", String.join(", ", context.getFavorPositions()));

        builder.append("\n## Resume Snapshots\n");
        if (context.getResumes().isEmpty()) {
            builder.append("- No resume found.\n");
        }
        for (OfferAgentUserContext.ResumeSnapshot resume : context.getResumes()) {
            builder.append("- resumeId=").append(resume.getId())
                    .append(", createTime=").append(resume.getCreateTime()).append('\n')
                    .append(resume.getContentPreview()).append("\n\n");
        }

        builder.append("\n## Delivery History\n");
        if (context.getPosts().isEmpty()) {
            builder.append("- No delivery history found.\n");
        }
        for (OfferAgentUserContext.PostSnapshot post : context.getPosts()) {
            builder.append("- postId=").append(post.getId())
                    .append(", resumeId=").append(post.getResumeId())
                    .append(", school=").append(defaultText(post.getSchool()))
                    .append(", education=").append(defaultText(post.getEducation()))
                    .append(", targetPositions=").append(String.join(", ", post.getFavorPositions()))
                    .append('\n');
        }

        builder.append("\n## Guidance History\n");
        if (context.getGuidanceRequests().isEmpty()) {
            builder.append("- No HR guidance request found.\n");
        }
        for (OfferAgentUserContext.GuidanceSnapshot guidance : context.getGuidanceRequests()) {
            builder.append("- requestId=").append(guidance.getId())
                    .append(", targetPosition=").append(defaultText(guidance.getTargetPosition()))
                    .append(", type=").append(defaultText(guidance.getGuidanceType()))
                    .append(", status=").append(defaultText(guidance.getStatus()))
                    .append(", requestTime=").append(guidance.getRequestTime())
                    .append(", feedback=").append(defaultText(guidance.getFeedbackPreview()))
                    .append('\n');
        }

        builder.append("\n## Retrieved Knowledge\n");
        if (context.getRetrievedKnowledge().isEmpty()) {
            builder.append("- No knowledge chunk retrieved.\n");
        }
        for (OfferAgentUserContext.KnowledgeSnapshot knowledge : context.getRetrievedKnowledge()) {
            builder.append("- title=").append(defaultText(knowledge.getTitle()))
                    .append(", category=").append(defaultText(knowledge.getCategory()))
                    .append(", tags=").append(defaultText(knowledge.getTags()))
                    .append(", targetPosition=").append(defaultText(knowledge.getTargetPosition()))
                    .append(", score=").append(defaultText(knowledge.getScore()))
                    .append(", mode=").append(defaultText(knowledge.getRetrievalMode()))
                    .append(", keywordScore=").append(defaultText(knowledge.getKeywordScore()))
                    .append(", vectorScore=").append(defaultText(knowledge.getVectorScore()))
                    .append(", source=").append(defaultText(knowledge.getSource()))
                    .append('\n')
                    .append("summary=").append(defaultText(knowledge.getSummary())).append('\n')
                    .append("content=").append(defaultText(knowledge.getContentPreview())).append("\n\n");
        }

        return builder.toString();
    }

    private void loadUserProfile(OfferAgentUserContext context, Integer userId) {
        userRepository.findById(userId).ifPresent(user -> {
            context.setUsername(user.getUsername());
            context.getToolTrace().add("getUserProfileTool: loaded t_user role=" + roleText(user));
        });

        List<Info> infos = infoRepository.findByUserId(userId);
        if (!infos.isEmpty()) {
            Info info = infos.get(0);
            context.setRealName(info.getRealname());
            context.setPhone(info.getPhone());
            context.setEmail(info.getEmail());
        }
        context.getToolTrace().add("getBasicInfoTool: loaded " + infos.size() + " t_info record(s)");
    }

    private void loadSeekerProfile(OfferAgentUserContext context, Integer userId) {
        seekerInfoRepository.findByUserId(userId).ifPresent(seekerInfo -> {
            context.setEducation(educationText(seekerInfo.getEducation()));
            context.setSchool(seekerInfo.getSchool());
            context.setMembership(seekerInfo.getMembership());
            context.setFavorPositions(parseFavor(seekerInfo.getFavor()));
        });
        context.getToolTrace().add("getSeekerProfileTool: loaded t_seeker_info for current user");
    }

    private void loadResumes(OfferAgentUserContext context, Integer userId) {
        List<Resume> resumes = resumeRepository.findByUserId(userId);
        for (Resume resume : resumes) {
            OfferAgentUserContext.ResumeSnapshot snapshot = new OfferAgentUserContext.ResumeSnapshot();
            snapshot.setId(resume.getId());
            snapshot.setCreateTime(resume.getCreateTime());
            snapshot.setContentPreview(clip(resume.getContent(), RESUME_PREVIEW_LIMIT));
            context.getResumes().add(snapshot);
        }
        context.getToolTrace().add("getResumeTool: loaded " + resumes.size() + " resume(s)");
    }

    private void loadPostHistory(OfferAgentUserContext context, Integer userId) {
        List<Post> posts = postRepository.findByUserIdWithDetails(userId);
        for (Post post : posts) {
            OfferAgentUserContext.PostSnapshot snapshot = new OfferAgentUserContext.PostSnapshot();
            snapshot.setId(post.getId());
            snapshot.setResumeId(post.getResumeId());
            if (post.getSeekerInfo() != null) {
                snapshot.setSchool(post.getSeekerInfo().getSchool());
                snapshot.setEducation(educationText(post.getSeekerInfo().getEducation()));
                snapshot.setFavorPositions(parseFavor(post.getSeekerInfo().getFavor()));
            }
            context.getPosts().add(snapshot);
        }
        context.getToolTrace().add("getDeliveryHistoryTool: loaded " + posts.size() + " delivery record(s)");
    }

    private void loadGuidanceHistory(OfferAgentUserContext context, Integer userId) {
        List<GuidanceRequest> requests = guidanceRequestRepository
                .findByUserIdAndDeletedAtIsNullOrderByRequestTimeDesc(userId.longValue());
        for (GuidanceRequest request : requests) {
            OfferAgentUserContext.GuidanceSnapshot snapshot = new OfferAgentUserContext.GuidanceSnapshot();
            snapshot.setId(request.getId());
            snapshot.setTargetPosition(request.getTargetPosition());
            snapshot.setGuidanceType(request.getGuidanceType());
            snapshot.setStatus(request.getStatus());
            snapshot.setRequestTime(request.getRequestTime());
            snapshot.setFeedbackPreview(clip(request.getFeedback(), FEEDBACK_PREVIEW_LIMIT));
            context.getGuidanceRequests().add(snapshot);
        }
        context.getToolTrace().add("getGuidanceHistoryTool: loaded " + requests.size() + " guidance request(s)");
    }

    private void loadRetrievedKnowledge(OfferAgentUserContext context, Integer userId, String message) {
        String queryText = buildRetrievalQuery(context, message);
        List<OfferAgentKnowledgeResult> results = knowledgeService.retrieve(userId, queryText);
        for (OfferAgentKnowledgeResult result : results) {
            OfferAgentUserContext.KnowledgeSnapshot snapshot = new OfferAgentUserContext.KnowledgeSnapshot();
            snapshot.setChunkId(result.getChunkId());
            snapshot.setDocId(result.getDocId());
            snapshot.setTitle(result.getTitle());
            snapshot.setCategory(result.getCategory());
            snapshot.setTags(result.getTags());
            snapshot.setTargetPosition(result.getTargetPosition());
            snapshot.setSource(result.getSource());
            snapshot.setSummary(result.getSummary());
            snapshot.setContentPreview(clip(result.getContent(), 900));
            snapshot.setScore(result.getScore());
            snapshot.setKeywordScore(result.getKeywordScore());
            snapshot.setVectorScore(result.getVectorScore());
            snapshot.setHybridScore(result.getHybridScore());
            snapshot.setRetrievalMode(result.getRetrievalMode());
            context.getRetrievedKnowledge().add(snapshot);
        }
        context.getToolTrace().add("retrieveKnowledgeTool: matched " + results.size() + " knowledge chunk(s)");
    }

    private String buildRetrievalQuery(OfferAgentUserContext context, String message) {
        StringBuilder builder = new StringBuilder();
        if (message != null) {
            builder.append(message).append(' ');
        }
        builder.append(defaultText(context.getSchool())).append(' ');
        builder.append(defaultText(context.getEducation())).append(' ');
        builder.append(String.join(" ", context.getFavorPositions())).append(' ');
        for (OfferAgentUserContext.ResumeSnapshot resume : context.getResumes()) {
            builder.append(defaultText(resume.getContentPreview())).append(' ');
        }
        return builder.toString();
    }

    private List<String> parseFavor(String favor) {
        if (favor == null || favor.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> values = objectMapper.readValue(favor, new TypeReference<List<String>>() {});
            return values == null ? new ArrayList<>() : values;
        } catch (Exception ignored) {
            return List.of(favor);
        }
    }

    private String clip(String value, int limit) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...[truncated]";
    }

    private void appendLine(StringBuilder builder, String key, Object value) {
        builder.append("- ").append(key).append(": ").append(defaultText(value)).append('\n');
    }

    private String defaultText(Object value) {
        if (value == null) {
            return "unknown";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "unknown" : text;
    }

    private String membershipText(Integer membership) {
        if (membership == null) {
            return "unknown";
        }
        return membership == 1 ? "premium" : "normal";
    }

    private String educationText(Integer education) {
        if (education == null) {
            return "unknown";
        }
        return switch (education) {
            case 1 -> "primary school";
            case 2 -> "middle school";
            case 3 -> "high school";
            case 4 -> "junior college";
            case 5 -> "bachelor";
            case 6 -> "master";
            case 7 -> "doctor";
            default -> "unknown";
        };
    }

    private String roleText(User user) {
        return switch (user.getRole()) {
            case 0 -> "admin";
            case 1 -> "jobseeker";
            case 2 -> "hr";
            default -> "unknown";
        };
    }
}
