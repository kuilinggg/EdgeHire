package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GenerateInterviewPlanTool implements OfferAgentTool {
    @Override
    public String name() {
        return "generate_interview_plan";
    }

    @Override
    public String description() {
        return "Generate deterministic interview preparation plan for target role.";
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("targetRole", "AI Agent intern");
        output.put("focusAreas", List.of("RAG retrieval flow", "Tool Calling safety", "Agent workflow design", "Spring Boot integration"));
        output.put("questions", List.of(
                "为什么 OfferAgent 需要 RAG，而不是直接把用户问题交给大模型？",
                "Tool Calling 如何映射到确定性的业务接口？",
                "关键词检索和向量检索各自解决什么问题？",
                "如果检索不到相关 chunk，系统如何降级？"
        ));
        output.put("sevenDayPlan", List.of(
                "Day 1: 梳理项目架构和数据流",
                "Day 2: 复盘 RAG 文档切分、召回、排序",
                "Day 3: 准备 Tool Calling 工具注册和执行流程",
                "Day 4: 准备 Spring Boot/JPA/Redis 问题",
                "Day 5: 模拟 AI Agent 项目深挖",
                "Day 6: 优化简历项目表述",
                "Day 7: 完整演示和自我介绍串联"
        ));
        output.put("summary", "generated deterministic AI Agent interview plan");
        return output;
    }
}
