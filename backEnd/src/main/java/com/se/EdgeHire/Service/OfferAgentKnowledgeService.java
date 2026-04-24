package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentKnowledgeResult;
import com.se.EdgeHire.DTO.OfferAgentVectorSearchResult;
import com.se.EdgeHire.Entity.OfferAgentKnowledgeChunk;
import com.se.EdgeHire.Entity.OfferAgentKnowledgeDoc;
import com.se.EdgeHire.Entity.OfferAgentRetrievalLog;
import com.se.EdgeHire.Repository.OfferAgentKnowledgeChunkRepository;
import com.se.EdgeHire.Repository.OfferAgentRetrievalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferAgentKnowledgeService {
    private static final int DEFAULT_TOP_K = 5;
    private static final int CANDIDATE_TOP_K = 10;
    private static final double VECTOR_WEIGHT = 0.7;
    private static final double KEYWORD_WEIGHT = 0.3;

    private final OfferAgentKnowledgeChunkRepository chunkRepository;
    private final OfferAgentRetrievalLogRepository retrievalLogRepository;
    private final OfferAgentVectorStoreService vectorStoreService;

    public List<OfferAgentKnowledgeResult> retrieve(Integer userId, String queryText) {
        List<OfferAgentKnowledgeResult> results = retrieve(queryText, DEFAULT_TOP_K);
        saveRetrievalLog(userId, queryText, results);
        return results;
    }

    public List<OfferAgentKnowledgeResult> retrieve(String queryText, int topK) {
        List<OfferAgentKnowledgeResult> candidates = loadKnowledgeCandidates();
        ensureVectorIndex(candidates);

        Map<Long, OfferAgentKnowledgeResult> merged = new HashMap<>();
        List<OfferAgentKnowledgeResult> keywordResults = keywordRetrieve(candidates, queryText, CANDIDATE_TOP_K);
        List<OfferAgentKnowledgeResult> vectorResults = vectorRetrieve(candidates, queryText, CANDIDATE_TOP_K);

        for (OfferAgentKnowledgeResult result : keywordResults) {
            merged.put(result.getChunkId(), copy(result));
        }
        for (OfferAgentKnowledgeResult result : vectorResults) {
            OfferAgentKnowledgeResult existing = merged.get(result.getChunkId());
            if (existing == null) {
                merged.put(result.getChunkId(), copy(result));
            } else {
                existing.setVectorScore(result.getVectorScore());
            }
        }

        return merged.values().stream()
                .peek(this::calculateHybridScore)
                .sorted(Comparator.comparing(OfferAgentKnowledgeResult::getHybridScore).reversed()
                        .thenComparing(OfferAgentKnowledgeResult::getTitle))
                .limit(topK)
                .collect(Collectors.toList());
    }

    public int rebuildVectorIndex() {
        return vectorStoreService.rebuild(loadKnowledgeCandidates());
    }

    public int vectorIndexSize() {
        if (vectorStoreService.size() == 0) {
            rebuildVectorIndex();
        }
        return vectorStoreService.size();
    }

    private List<OfferAgentKnowledgeResult> keywordRetrieve(
            List<OfferAgentKnowledgeResult> candidates,
            String queryText,
            int topK
    ) {
        List<String> queryTerms = extractTerms(queryText);
        return candidates.stream()
                .map(candidate -> {
                    OfferAgentKnowledgeResult result = copy(candidate);
                    int keywordScore = keywordScore(result, queryTerms);
                    result.setKeywordScore(keywordScore);
                    result.setRetrievalMode("keyword");
                    return result;
                })
                .filter(result -> result.getKeywordScore() != null && result.getKeywordScore() > 0)
                .sorted(Comparator.comparing(OfferAgentKnowledgeResult::getKeywordScore).reversed()
                        .thenComparing(OfferAgentKnowledgeResult::getTitle))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private List<OfferAgentKnowledgeResult> vectorRetrieve(
            List<OfferAgentKnowledgeResult> candidates,
            String queryText,
            int topK
    ) {
        Map<Long, OfferAgentKnowledgeResult> candidateMap = candidates.stream()
                .collect(Collectors.toMap(OfferAgentKnowledgeResult::getChunkId, this::copy, (left, right) -> left));
        try {
            return vectorStoreService.search(queryText, topK).stream()
                    .map(vectorResult -> toVectorResult(candidateMap, vectorResult))
                    .filter(result -> result != null && result.getVectorScore() != null && result.getVectorScore() > 0)
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private OfferAgentKnowledgeResult toVectorResult(
            Map<Long, OfferAgentKnowledgeResult> candidateMap,
            OfferAgentVectorSearchResult vectorResult
    ) {
        OfferAgentKnowledgeResult result = candidateMap.get(vectorResult.getChunkId());
        if (result == null) {
            return null;
        }
        result.setVectorScore(vectorResult.getSimilarity());
        result.setRetrievalMode("vector");
        return result;
    }

    private void calculateHybridScore(OfferAgentKnowledgeResult result) {
        int keywordScore = result.getKeywordScore() == null ? 0 : result.getKeywordScore();
        double normalizedKeywordScore = Math.min(keywordScore / 20.0, 1.0);
        double vectorScore = result.getVectorScore() == null ? 0.0 : result.getVectorScore();
        double hybridScore = vectorScore > 0
                ? vectorScore * VECTOR_WEIGHT + normalizedKeywordScore * KEYWORD_WEIGHT
                : normalizedKeywordScore;

        result.setHybridScore(hybridScore);
        result.setScore((int) Math.round(hybridScore * 100));
        if (keywordScore > 0 && vectorScore > 0) {
            result.setRetrievalMode("hybrid");
        } else if (vectorScore > 0) {
            result.setRetrievalMode("vector");
        } else {
            result.setRetrievalMode("keyword");
        }
    }

    private void ensureVectorIndex(List<OfferAgentKnowledgeResult> candidates) {
        if (vectorStoreService.size() == 0) {
            vectorStoreService.rebuild(candidates);
        }
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
            log.setRetrievalMode("hybrid");
            retrievalLogRepository.save(log);
        } catch (Exception ignored) {
            // Retrieval logging is useful, but a missing log table must not block the assistant.
        }
    }

    private int keywordScore(OfferAgentKnowledgeResult result, List<String> queryTerms) {
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
                "后端", "实习", "简历", "面试", "项目", "STAR", "向量", "Embedding",
                "VectorStore", "多Agent", "武汉大学", "软件工程", "工具调用", "知识库"
        );
        String lowerQuery = queryText.toLowerCase(Locale.ROOT);
        for (String term : domainTerms) {
            if (lowerQuery.contains(term.toLowerCase(Locale.ROOT))) {
                terms.add(term);
            }
        }

        return new ArrayList<>(terms);
    }

    private OfferAgentKnowledgeResult copy(OfferAgentKnowledgeResult source) {
        OfferAgentKnowledgeResult result = new OfferAgentKnowledgeResult();
        result.setChunkId(source.getChunkId());
        result.setDocId(source.getDocId());
        result.setTitle(source.getTitle());
        result.setCategory(source.getCategory());
        result.setTags(source.getTags());
        result.setTargetPosition(source.getTargetPosition());
        result.setSource(source.getSource());
        result.setSummary(source.getSummary());
        result.setContent(source.getContent());
        result.setScore(source.getScore());
        result.setKeywordScore(source.getKeywordScore());
        result.setVectorScore(source.getVectorScore());
        result.setHybridScore(source.getHybridScore());
        result.setRetrievalMode(source.getRetrievalMode());
        return result;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private OfferAgentKnowledgeResult builtin(
            Long chunkId,
            String title,
            String category,
            String tags,
            String targetPosition,
            String summary,
            String content
    ) {
        OfferAgentKnowledgeResult result = new OfferAgentKnowledgeResult();
        result.setChunkId(chunkId);
        result.setDocId(chunkId);
        result.setTitle(title);
        result.setCategory(category);
        result.setTags(tags);
        result.setTargetPosition(targetPosition);
        result.setSource("builtin");
        result.setSummary(summary);
        result.setContent(content);
        result.setScore(0);
        result.setKeywordScore(0);
        result.setVectorScore(0.0);
        result.setHybridScore(0.0);
        result.setRetrievalMode("none");
        return result;
    }

    private List<OfferAgentKnowledgeResult> builtinKnowledge() {
        return List.of(
                builtin(
                        -1L,
                        "AI Agent internship competency model",
                        "role-model",
                        "AI Agent,RAG,Tool Calling,multi-agent,internship",
                        "AI Agent intern",
                        "AI Agent roles value LLM application engineering, RAG, tool calling, workflow orchestration, streaming output, and evaluation.",
                        "An AI Agent intern should understand LLM application pipelines and be able to integrate model calls with business systems. Strong projects explain how user data is retrieved, how knowledge is searched, how tools map to business APIs, how multiple agents divide responsibilities, how errors are handled, and how logs or evaluations prove the workflow is reliable."
                ),
                builtin(
                        -2L,
                        "Software engineering student resume rules",
                        "resume",
                        "resume,software engineering,project experience,STAR,internship",
                        "software engineering intern",
                        "Student resumes should turn courses, projects, and internship goals into verifiable engineering evidence.",
                        "A software engineering student resume should not only list technology stacks. It should explain the project background, owned module, technical design, hard problem, and result. STAR is useful: situation, task, action, result. If there are no business metrics, mention feature scope, API count, database tables, test coverage, response mode, and demo pages."
                ),
                builtin(
                        -3L,
                        "RAG project resume expression guide",
                        "RAG",
                        "RAG,knowledge base,retrieval,Embedding,VectorStore,keyword retrieval",
                        "AI Agent intern",
                        "A RAG project should describe chunking, recall, ranking, context injection, retrieval logs, and source display.",
                        "A RAG project can be described in stages: first use MySQL document tables plus keyword and tag retrieval to complete the retrieval augmented generation loop; then add Embedding and VectorStore to improve semantic recall; finally add retrieval logs, cited sources, and evaluation sets. Explain why lightweight keyword RAG was implemented first and how the architecture can evolve to vector retrieval."
                ),
                builtin(
                        -4L,
                        "Java backend internship competency model",
                        "role-model",
                        "Java,Spring Boot,JPA,Redis,MySQL,backend,internship",
                        "Java backend intern",
                        "Java backend roles focus on API design, database modeling, caching, transactions, and maintainability.",
                        "Java backend intern projects should explain Spring Boot REST APIs, Controller-Service-Repository layering, JPA or MyBatis usage, MySQL table design, Redis caching, authentication, error handling, and tests. For this project, mention DTOs, why entities are not directly exposed in cache, stream endpoints, and how the new OfferAgent module avoids breaking original resume optimization."
                ),
                builtin(
                        -5L,
                        "AI Agent interview preparation checklist",
                        "interview",
                        "interview,AI Agent,RAG,Tool Calling,Prompt,evaluation",
                        "AI Agent intern",
                        "Interview preparation should cover architecture, agent responsibilities, RAG retrieval, tool safety, and quality evaluation.",
                        "Common AI Agent interview questions include: why RAG is needed, how documents are chunked, how Top-K is chosen, what happens when retrieval misses, how Tool Calling maps to business APIs, whether multi-agent execution is actually parallel, how prompts reduce hallucination, how retrieval logs are recorded, and how answer quality is evaluated."
                )
        );
    }
}
