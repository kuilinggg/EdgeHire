package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolParameter;
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
        return "为目标岗位生成确定性的中文面试准备计划。";
    }

    @Override
    public List<OfferAgentToolParameter> parameters() {
        return List.of(
                new OfferAgentToolParameter("targetPosition", "string", "目标岗位名称。", false),
                new OfferAgentToolParameter("days", "integer", "准备计划天数。", false)
        );
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("targetRole", context.getArguments().getOrDefault("targetPosition", "AI Agent 实习生"));
        output.put("focusAreas", List.of("RAG 检索链路", "Tool Calling 安全边界", "多 Agent 工作流设计", "Spring Boot 工程集成"));
        output.put("questions", List.of(
                "为什么 OfferAgent 需要 RAG，而不是直接把用户问题交给大模型？",
                "Tool Calling 如何映射到确定性的业务接口，并避免模型越权调用？",
                "关键词检索和向量检索分别解决什么问题？为什么要做混合检索？",
                "如果 RAG 没有命中相关知识片段，系统应该如何降级？",
                "多 Agent 工作流中每个 Agent 的边界是什么？为什么这样拆分？"
        ));
        output.put("sevenDayPlan", List.of(
                "第 1 天：梳理项目架构、数据库表和核心数据流。",
                "第 2 天：复盘 RAG 的文档切分、召回、重排和降级策略。",
                "第 3 天：准备 Tool Calling 的工具注册、参数校验、日志追踪和 fallback 机制。",
                "第 4 天：准备 Spring Boot、JPA、Redis、WebClient 等工程实现问题。",
                "第 5 天：围绕多 Agent 工作流准备项目深挖问答。",
                "第 6 天：优化简历中的项目描述，补充量化结果和工程细节。",
                "第 7 天：完整演示系统，并练习 1 分钟和 3 分钟项目介绍。"
        ));
        output.put("summary", "已生成中文 AI Agent 面试准备计划");
        return output;
    }
}
