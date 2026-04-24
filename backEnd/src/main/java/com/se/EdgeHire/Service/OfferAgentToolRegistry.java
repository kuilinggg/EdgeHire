package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OfferAgentToolRegistry {
    private final Map<String, OfferAgentTool> tools;

    public OfferAgentToolRegistry(List<OfferAgentTool> toolList) {
        this.tools = toolList.stream()
                .collect(Collectors.toMap(OfferAgentTool::name, Function.identity()));
    }

    public Optional<OfferAgentTool> find(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public boolean contains(String name) {
        return tools.containsKey(name);
    }

    public List<String> names() {
        return tools.keySet().stream().sorted().toList();
    }

    public List<OfferAgentToolDefinition> definitions() {
        return tools.values().stream()
                .map(OfferAgentTool::definition)
                .sorted(java.util.Comparator.comparing(OfferAgentToolDefinition::getName))
                .toList();
    }
}
