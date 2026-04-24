package com.se.EdgeHire.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.Entity.SeekerInfo;
import com.se.EdgeHire.Repository.SeekerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GetJobIntentionTool implements OfferAgentTool {
    private final SeekerInfoRepository seekerInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String name() {
        return "get_job_intention";
    }

    @Override
    public String description() {
        return "Read target positions, school, education, and membership from t_seeker_info.";
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        Map<String, Object> output = new LinkedHashMap<>();
        seekerInfoRepository.findByUserId(context.getUserId()).ifPresent(info -> fill(output, info));
        output.putIfAbsent("targetPositions", List.of());
        output.put("summary", "loaded deterministic job intention");
        return output;
    }

    private void fill(Map<String, Object> output, SeekerInfo info) {
        output.put("education", info.getEducation());
        output.put("educationText", educationText(info.getEducation()));
        output.put("school", info.getSchool());
        output.put("membership", info.getMembership());
        output.put("targetPositions", parseFavor(info.getFavor()));
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

    private String educationText(Integer education) {
        if (education == null) return "unknown";
        return switch (education) {
            case 5 -> "bachelor";
            case 6 -> "master";
            case 7 -> "doctor";
            default -> String.valueOf(education);
        };
    }
}
