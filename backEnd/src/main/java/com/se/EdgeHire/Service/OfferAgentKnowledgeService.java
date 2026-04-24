package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.Entity.OfferAgentKnowledgeChunk;
import com.se.EdgeHire.Entity.OfferAgentKnowledgeDoc;
import com.se.EdgeHire.Entity.OfferAgentRetrievalLog;
import com.se.EdgeHire.Repository.OfferAgentKnowledgeChunkRepository;
import com.se.EdgeHire.Repository.OfferAgentRetrievalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferAgentKnowledgeService {
    private static final int DEFAULT_TOP_K = 5;

    private final OfferAgentKnowledgeChunkRepository chunkRepository;
    private final OfferAgentRetrievalLogRepository retrievalLogRepository;

    public List<OfferAgentKnowledgeResult> retrieve(Integer userId, String queryText) {
        List<OfferAgentKnowledgeResult> results = retrieve(queryText, DEFAULT_TOP_K);
        saveRetrievalLog(userId, queryText, results);
        return results;
    }

    public List<OfferAgentKnowledgeResult> retrieve(String queryText, int topK) {
        List<String> queryTerms = extractTerms(queryText);
        List<OfferAgentKnowledgeResult> candidates = new ArrayList<>();

        for (OfferAgentKnowledgeResult result : loadKnowledgeCandidates()) {
            int score = score(result, queryTerms);
            if (score > 0) {
                result.setScore(score);
                candidates.add(result);
            }
        }

        return candidates.stream()
                .sorted(Comparator.comparing(OfferAgentKnowledgeResult::getScore).reversed()
                        .thenComparing(OfferAgentKnowledgeResult::getTitle))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private List<OfferAgentKnowledgeResult> loadKnowledgeCandidates() {
        List<OfferAgentKnowledgeResult> results = new ArrayList<>(builtinKnowledge());
        try {
            for (OfferAgentKnowledgeChunk chunk : chunkRepository.findAllWithDocument()) {
                OfferAgentKnowledgeDoc doc = chunk.getDocument();
                OfferAgentKnowledgeResult result = new OfferAgentKnowledgeResult();
                result.setChunkId(chunk.getId());
                result.setDocId(chunk.getDocId());
                result.setTitle(doc == null ? "Untitled" : doc.getTitle());
                result.setCategory(doc == null ? "" : doc.getCategory());
                result.setTags(doc == null ? "" : doc.getTags());
                result.setTargetPosition(doc == null ? "" : doc.getTargetPosition());
                result.setSource(doc == null ? "database" : doc.getSource());
                result.setSummary(chunk.getSummary());
                result.setContent(chunk.getContent());
                result.setScore(0);
                results.add(result);
            }
        } catch (Exception ignored) {
            // Database tables may not be created yet in a local demo. Built-in documents keep RAG usable.
        }
        return results;
    }

    private void saveRetrievalLog(Integer userId, String queryText, List<OfferAgentKnowledgeResult> results) {
        if (userId == null) {
            return;
        }
        try {
            OfferAgentRetrievalLog log = new OfferAgentRetrievalLog();
            log.setUserId(userId);
            log.setQueryText(queryText == null ? "" : queryText);
            log.setMatchedChunkIds(results.stream()
                    .map(result -> String.valueOf(result.getChunkId()))
                    .collect(Collectors.joining(",")));
            log.setTopScore(results.isEmpty() ? 0 : results.get(0).getScore());
            retrievalLogRepository.save(log);
        } catch (Exception ignored) {
            // Retrieval logging is useful, but a missing log table must not block the assistant.
        }
    }

    private int score(OfferAgentKnowledgeResult result, List<String> queryTerms) {
        String title = lower(result.getTitle());
        String tags = lower(result.getTags());
        String target = lower(result.getTargetPosition());
        String summary = lower(result.getSummary());
        String content = lower(result.getContent());
        int score = 0;

        for (String term : queryTerms) {
            if (term.isBlank()) {
                continue;
            }
            String normalized = term.toLowerCase(Locale.ROOT);
            if (title.contains(normalized)) {
                score += 6;
            }
            if (tags.contains(normalized)) {
                score += 5;
            }
            if (target.contains(normalized)) {
                score += 5;
            }
            if (summary.contains(normalized)) {
                score += 3;
            }
            if (content.contains(normalized)) {
                score += 2;
            }
        }

        return score;
    }

    private List<String> extractTerms(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return List.of();
        }
        Set<String> terms = new LinkedHashSet<>();
        String normalized = queryText
                .replaceAll("[{}\\[\\]\"':,，。；;()（）|/\\\\]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        for (String token : normalized.split(" ")) {
            if (token.length() >= 2) {
                terms.add(token);
            }
        }

        List<String> domainTerms = List.of(
                "AI Agent", "Agent", "RAG", "Tool Calling", "Spring Boot", "Java",
                "后端", "实习", "简历", "面试", "项目", "STAR", "向量", "多Agent",
                "武汉大学", "软件工程"
        );
        String lowerQuery = queryText.toLowerCase(Locale.ROOT);
        for (String term : domainTerms) {
            if (lowerQuery.contains(term.toLowerCase(Locale.ROOT))) {
                terms.add(term);
            }
        }

        return new ArrayList<>(terms);
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private List<OfferAgentKnowledgeResult> builtinKnowledge() {
        return List.of(
                new OfferAgentKnowledgeResult(
                        -1L,
                        -1L,
                        "AI Agent 实习岗位能力模型",
                        "岗位能力模型",
                        "AI Agent,RAG,Tool Calling,多Agent,实习",
                        "AI Agent 实习生",
                        "builtin",
                        "AI Agent 实习通常关注大模型应用开发、工具调用、RAG、Agent 编排和工程落地能力。",
                        "AI Agent 实习岗位一般要求候选人理解大模型应用链路，能够使用 Java 或 Python 完成业务系统集成，熟悉 RAG 检索增强、Tool Calling、Prompt 编排、多 Agent 工作流、流式输出和基础评测。简历中应突出项目如何读取业务数据、如何检索知识、如何调用工具、如何形成可执行计划，以及如何处理错误、权限和日志。",
                        0
                ),
                new OfferAgentKnowledgeResult(
                        -2L,
                        -2L,
                        "软件工程学生简历优化规则",
                        "简历优化",
                        "简历,软件工程,项目经历,STAR,实习",
                        "软件工程实习生",
                        "builtin",
                        "学生简历要把课程、项目和实习意向转成可验证的工程能力证据。",
                        "软件工程学生投实习时，简历重点不应只写技术栈列表，而要写清楚项目背景、本人负责模块、技术方案、遇到的问题和结果。项目描述建议使用 STAR：场景、任务、行动、结果。没有真实业务指标时，可以写清楚功能规模、接口数量、数据表设计、测试覆盖、响应方式和可演示页面。",
                        0
                ),
                new OfferAgentKnowledgeResult(
                        -3L,
                        -3L,
                        "RAG 项目简历表达建议",
                        "RAG",
                        "RAG,知识库,检索,Embedding,向量数据库,关键词检索",
                        "AI Agent 实习生",
                        "builtin",
                        "RAG 项目表达要覆盖文档切分、召回、排序、上下文注入和引用来源展示。",
                        "RAG 项目可以分阶段表达：第一阶段使用 MySQL 文档表和关键词/标签召回，完成检索增强闭环；第二阶段加入 Embedding 和 VectorStore，提高语义召回能力；第三阶段增加检索日志、引用来源和评测集。面试时应说明为什么先做轻量关键词 RAG，以及如何演进到向量检索。",
                        0
                ),
                new OfferAgentKnowledgeResult(
                        -4L,
                        -4L,
                        "Java 后端实习岗位能力模型",
                        "岗位能力模型",
                        "Java,Spring Boot,JPA,Redis,MySQL,后端,实习",
                        "Java 后端实习生",
                        "builtin",
                        "Java 后端实习关注接口设计、数据库建模、缓存、事务和可维护性。",
                        "Java 后端实习岗位通常关注 Spring Boot、RESTful API、JPA/MyBatis、MySQL 表设计、Redis 缓存、鉴权、异常处理和测试。简历项目中应说明 Controller-Service-Repository 分层、DTO 的作用、为什么避免直接暴露实体、如何做流式接口和如何保证原有功能不被新模块影响。",
                        0
                ),
                new OfferAgentKnowledgeResult(
                        -5L,
                        -5L,
                        "AI Agent 面试准备清单",
                        "面试准备",
                        "面试,AI Agent,RAG,Tool Calling,Prompt,评测",
                        "AI Agent 实习生",
                        "builtin",
                        "面试准备应覆盖项目架构、Agent 分工、RAG 召回、工具安全和效果评估。",
                        "AI Agent 项目面试常见问题包括：为什么需要 RAG，如何切分文档，如何决定 Top-K，如何处理检索不到内容，Tool Calling 如何和业务接口对应，多 Agent 是否真的并行，Prompt 如何约束幻觉，如何记录检索日志，如何评估回答质量。回答时要结合系统中的真实数据流说明。",
                        0
                )
        );
    }
}
