package com.se.EdgeHire.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.EdgeHire.DTO.OfferAgentEvaluationMetric;
import com.se.EdgeHire.DTO.OfferAgentEvaluationRequest;
import com.se.EdgeHire.DTO.OfferAgentEvaluationResponse;
import com.se.EdgeHire.DTO.OfferAgentLlmJudgeResult;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferAgentEvaluationService {
    private static final List<String> ACTION_WORDS = List.of(
            "下一步", "建议", "计划", "准备", "优化", "补充", "练习", "投递", "修改", "行动"
    );
    private static final List<String> EVIDENCE_WORDS = List.of(
            "简历", "岗位", "技能", "项目", "RAG", "工具", "匹配", "面试", "学校", "投递", "知识库", "来源"
    );

    private final OfferAgentLlmJudgeClient llmJudgeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OfferAgentEvaluationResponse evaluate(OfferAgentEvaluationRequest request) {
        OfferAgentToolExecutionReport report = request.getToolReport() == null
                ? new OfferAgentToolExecutionReport()
                : request.getToolReport();
        List<OfferAgentEvaluationMetric> ruleMetrics = List.of(
                evaluateRag(report, request.getFinalAnswer()),
                evaluateTools(report),
                evaluateAnswerQuality(request.getFinalAnswer()),
                evaluateTargetAlignment(request.getTargetPosition(), request.getMessage(), request.getFinalAnswer())
        );
        int ruleScore = weightedScore(ruleMetrics);
        String ruleGrade = grade(ruleScore);

        EvaluationNarrative ruleNarrative = buildRuleNarrative(ruleMetrics);
        Optional<OfferAgentLlmJudgeResult> judgeResult = llmJudgeClient.judge(request, ruleMetrics, ruleScore, ruleGrade);
        if (judgeResult.isEmpty()) {
            return buildRuleOnlyResponse(ruleScore, ruleGrade, ruleMetrics, ruleNarrative);
        }

        OfferAgentLlmJudgeResult judge = normalizeJudgeResult(judgeResult.get());
        int finalScore = Math.round((float) (ruleScore * 0.65 + judge.getScore() * 0.35));
        String finalGrade = grade(finalScore);

        List<String> strengths = merge(ruleNarrative.strengths(), judge.getStrengths());
        List<String> risks = merge(ruleNarrative.risks(), judge.getRisks());
        List<String> suggestions = merge(ruleNarrative.suggestions(), judge.getSuggestions());

        OfferAgentEvaluationResponse response = new OfferAgentEvaluationResponse(
                finalScore,
                finalGrade,
                ruleMetrics,
                strengths,
                risks,
                suggestions
        );
        response.setRuleScore(ruleScore);
        response.setRuleGrade(ruleGrade);
        response.setLlmJudgeScore(judge.getScore());
        response.setLlmJudgeGrade(judge.getGrade());
        response.setEvaluationMode("hybrid_rule_llm");
        response.setLlmJudge(judge);
        return response;
    }

    private OfferAgentEvaluationResponse buildRuleOnlyResponse(
            int ruleScore,
            String ruleGrade,
            List<OfferAgentEvaluationMetric> ruleMetrics,
            EvaluationNarrative ruleNarrative) {
        OfferAgentEvaluationResponse response = new OfferAgentEvaluationResponse(
                ruleScore,
                ruleGrade,
                ruleMetrics,
                ruleNarrative.strengths(),
                ruleNarrative.risks(),
                ruleNarrative.suggestions()
        );
        response.setRuleScore(ruleScore);
        response.setRuleGrade(ruleGrade);
        response.setLlmJudgeScore(null);
        response.setLlmJudgeGrade(null);
        response.setEvaluationMode("rule_only_fallback");
        response.setLlmJudge(OfferAgentLlmJudgeResult.unavailable("ai-module judge endpoint unavailable or returned invalid result."));
        return response;
    }

    private int weightedScore(List<OfferAgentEvaluationMetric> metrics) {
        return Math.round((float) (
                metrics.get(0).getScore() * 0.25
                        + metrics.get(1).getScore() * 0.25
                        + metrics.get(2).getScore() * 0.30
                        + metrics.get(3).getScore() * 0.20
        ));
    }

    private EvaluationNarrative buildRuleNarrative(List<OfferAgentEvaluationMetric> metrics) {
        List<String> strengths = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (OfferAgentEvaluationMetric metric : metrics) {
            if (metric.getScore() >= 80) {
                strengths.add(metric.getName() + "表现较好：" + metric.getSummary());
            } else if (metric.getScore() < 60) {
                risks.add(metric.getName() + "存在风险：" + metric.getSummary());
            }
        }
        addSuggestions(metrics, suggestions);
        return new EvaluationNarrative(strengths, risks, suggestions);
    }

    private OfferAgentEvaluationMetric evaluateRag(OfferAgentToolExecutionReport report, String answer) {
        List<OfferAgentToolResult> ragCalls = toolCalls(report, "retrieve_knowledge");
        if (ragCalls.isEmpty()) {
            return metric("RAG 检索证据", 35, "weak", "本轮没有调用知识检索工具", "未发现 retrieve_knowledge 调用");
        }
        int totalHits = ragCalls.stream().mapToInt(this::hitCount).sum();
        boolean answerMentionsRag = containsAny(answer, "RAG", "检索", "知识", "来源", "文档", "片段");
        int score = Math.min(100, 45 + totalHits * 8 + (answerMentionsRag ? 15 : 0));
        String summary = totalHits > 0
                ? "共召回 " + totalHits + " 个知识片段" + (answerMentionsRag ? "，回答中体现了检索依据" : "，但回答中的证据表达偏弱")
                : "调用了检索工具，但没有召回知识片段";
        return metric("RAG 检索证据", totalHits > 0 ? score : 45, status(score), summary, "retrieve_knowledge calls=" + ragCalls.size());
    }

    private OfferAgentEvaluationMetric evaluateTools(OfferAgentToolExecutionReport report) {
        List<OfferAgentToolResult> calls = report.getToolCalls() == null ? List.of() : report.getToolCalls();
        if (calls.isEmpty()) {
            return metric("Tool Calling 执行", 30, "weak", "本轮没有工具调用结果", "toolCalls=0");
        }
        long success = calls.stream().filter(call -> Boolean.TRUE.equals(call.getSuccess())).count();
        boolean hasMatch = calls.stream().anyMatch(call -> "calculate_job_match_score".equals(call.getToolName()));
        boolean hasResume = calls.stream().anyMatch(call -> "get_resume_summary".equals(call.getToolName()));
        boolean hasRag = calls.stream().anyMatch(call -> "retrieve_knowledge".equals(call.getToolName()));
        int score = (int) Math.round(success * 100.0 / calls.size());
        if (hasRag) score += 5;
        if (hasResume) score += 5;
        if (hasMatch) score += 5;
        score = Math.min(100, score);
        return metric(
                "Tool Calling 执行",
                score,
                status(score),
                "成功调用 " + success + "/" + calls.size() + " 个工具",
                "resume=" + hasResume + ", rag=" + hasRag + ", match=" + hasMatch
        );
    }

    private OfferAgentEvaluationMetric evaluateAnswerQuality(String answer) {
        String text = safe(answer);
        if (text.isBlank()) {
            return metric("最终回答质量", 20, "weak", "回答为空", "finalAnswer length=0");
        }
        int sectionCount = countOccurrences(text, "##");
        int actionHits = countContains(text, ACTION_WORDS);
        int evidenceHits = countContains(text, EVIDENCE_WORDS);
        int score = 35;
        if (text.length() >= 160) score += 15;
        if (sectionCount >= 2) score += 20;
        if (actionHits >= 3) score += 15;
        if (evidenceHits >= 3) score += 15;
        score = Math.min(100, score);
        return metric(
                "最终回答质量",
                score,
                status(score),
                "结构段落 " + sectionCount + " 个，行动建议命中 " + actionHits + " 类",
                "answerLength=" + text.length() + ", evidenceHits=" + evidenceHits
        );
    }

    private OfferAgentEvaluationMetric evaluateTargetAlignment(String targetPosition, String message, String answer) {
        String target = safe(targetPosition);
        if (target.isBlank()) {
            target = inferTarget(message);
        }
        if (target.isBlank()) {
            return metric("岗位一致性", 50, "medium", "未识别到明确目标岗位", "targetPosition is blank");
        }
        List<String> keywords = targetKeywords(target);
        int hits = countContains(answer, keywords);
        boolean targetMentioned = safe(answer).contains(target) || keywords.stream().anyMatch(keyword -> safe(answer).contains(keyword));
        int score = Math.min(100, 45 + hits * 12 + (targetMentioned ? 15 : 0));
        return metric(
                "岗位一致性",
                score,
                status(score),
                "目标岗位为「" + target + "」，回答命中 " + hits + " 个岗位关键词",
                "keywords=" + String.join(",", keywords)
        );
    }

    private List<OfferAgentToolResult> toolCalls(OfferAgentToolExecutionReport report, String toolName) {
        if (report.getToolCalls() == null) {
            return List.of();
        }
        return report.getToolCalls().stream()
                .filter(call -> toolName.equals(call.getToolName()))
                .toList();
    }

    private int hitCount(OfferAgentToolResult result) {
        try {
            JsonNode node = objectMapper.readTree(result.getOutputJson());
            return node.path("hitCount").asInt(0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void addSuggestions(List<OfferAgentEvaluationMetric> metrics, List<String> suggestions) {
        for (OfferAgentEvaluationMetric metric : metrics) {
            if (metric.getScore() >= 80) {
                continue;
            }
            switch (metric.getName()) {
                case "RAG 检索证据" -> suggestions.add("补充回答中的知识来源，并说明检索片段如何支撑结论。");
                case "Tool Calling 执行" -> suggestions.add("检查工具规划是否覆盖简历、岗位匹配和知识检索。");
                case "最终回答质量" -> suggestions.add("让回答保持清晰分段，增加可执行步骤和具体改写示例。");
                case "岗位一致性" -> suggestions.add("围绕目标岗位补充关键词、能力差距和投递建议。");
                default -> suggestions.add("补充更多可验证证据。");
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("本轮质量较好，可沉淀为优秀样例用于后续评测对比。");
        }
    }

    private OfferAgentLlmJudgeResult normalizeJudgeResult(OfferAgentLlmJudgeResult judge) {
        int score = clamp(judge.getScore() == null ? 0 : judge.getScore());
        judge.setAvailable(true);
        judge.setScore(score);
        judge.setGrade(safe(judge.getGrade()).isBlank() ? grade(score) : judge.getGrade());
        if (judge.getStrengths() == null) judge.setStrengths(new ArrayList<>());
        if (judge.getRisks() == null) judge.setRisks(new ArrayList<>());
        if (judge.getSuggestions() == null) judge.setSuggestions(new ArrayList<>());
        if (safe(judge.getSummary()).isBlank()) {
            judge.setSummary("LLM 裁判已完成语义质量评估。");
        }
        return judge;
    }

    private List<String> merge(List<String> left, List<String> right) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (left != null) merged.addAll(left);
        if (right != null) merged.addAll(right);
        return new ArrayList<>(merged);
    }

    private OfferAgentEvaluationMetric metric(String name, int score, String status, String summary, String evidence) {
        return new OfferAgentEvaluationMetric(name, clamp(score), status, summary, evidence);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private String status(int score) {
        if (score >= 80) return "strong";
        if (score >= 60) return "medium";
        return "weak";
    }

    private String grade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "E";
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = safe(text).indexOf(pattern);
        while (index >= 0) {
            count++;
            index = safe(text).indexOf(pattern, index + pattern.length());
        }
        return count;
    }

    private int countContains(String text, List<String> keywords) {
        String source = safe(text).toLowerCase(Locale.ROOT);
        int count = 0;
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase(Locale.ROOT))) {
                count++;
            }
        }
        return count;
    }

    private boolean containsAny(String text, String... keywords) {
        String source = safe(text).toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (source.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String inferTarget(String message) {
        String text = safe(message);
        if (containsAny(text, "前端", "vue", "react")) return "前端开发";
        if (containsAny(text, "后端", "java", "spring")) return "Java 后端";
        if (containsAny(text, "数据", "算法", "python")) return "数据算法";
        if (containsAny(text, "产品", "运营")) return "产品运营";
        if (containsAny(text, "agent", "rag", "大模型", "llm")) return "AI Agent";
        return "";
    }

    private List<String> targetKeywords(String target) {
        String text = target.toLowerCase(Locale.ROOT);
        if (containsAny(text, "前端", "vue", "react")) {
            return List.of("前端", "Vue", "React", "TypeScript", "JavaScript", "CSS", "Vite", "接口");
        }
        if (containsAny(text, "后端", "java", "spring")) {
            return List.of("后端", "Java", "Spring Boot", "JPA", "MySQL", "Redis", "接口", "JUnit");
        }
        if (containsAny(text, "数据", "算法", "python")) {
            return List.of("数据", "算法", "Python", "SQL", "Embedding", "召回", "评估", "A/B");
        }
        if (containsAny(text, "产品", "运营")) {
            return List.of("产品", "运营", "用户", "需求", "流程", "指标", "转化");
        }
        return List.of("AI Agent", "RAG", "Tool Calling", "多 Agent", "Prompt", "VectorStore", "Embedding");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record EvaluationNarrative(List<String> strengths, List<String> risks, List<String> suggestions) {
    }
}
