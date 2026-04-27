package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentEvaluationMetric;
import com.se.EdgeHire.DTO.OfferAgentEvaluationRequest;
import com.se.EdgeHire.DTO.OfferAgentEvaluationResponse;
import com.se.EdgeHire.DTO.OfferAgentLlmJudgeResult;
import com.se.EdgeHire.DTO.OfferAgentToolExecutionReport;
import com.se.EdgeHire.DTO.OfferAgentToolResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OfferAgentEvaluationServiceTest {

    @Test
    void evaluateReturnsHybridScoreWhenLlmJudgeIsAvailable() {
        OfferAgentEvaluationRequest request = strongRequest();
        OfferAgentEvaluationService service = new OfferAgentEvaluationService((ignoredRequest, metrics, ruleScore, ruleGrade) ->
                Optional.of(new OfferAgentLlmJudgeResult(
                        true,
                        88,
                        "B",
                        "回答能够结合 RAG 与工具结果，行动建议较具体。",
                        List.of("证据链比较完整"),
                        List.of("可以进一步补充岗位关键词优先级"),
                        List.of("在结尾增加可执行的投递清单"),
                        "{\"score\":88}",
                        null
                ))
        );

        OfferAgentEvaluationResponse response = service.evaluate(request);

        assertThat(response.getEvaluationMode()).isEqualTo("hybrid_rule_llm");
        assertThat(response.getRuleScore()).isGreaterThanOrEqualTo(80);
        assertThat(response.getLlmJudgeScore()).isEqualTo(88);
        assertThat(response.getOverallScore()).isBetween(response.getRuleScore() - 5, response.getLlmJudgeScore() + 5);
        assertThat(response.getLlmJudge().getSummary()).contains("RAG");
        assertThat(response.getSuggestions()).contains("在结尾增加可执行的投递清单");
    }

    @Test
    void evaluateFallsBackToRuleScoreWhenLlmJudgeIsUnavailable() {
        OfferAgentEvaluationRequest request = weakRequest();
        OfferAgentEvaluationService service = new OfferAgentEvaluationService((ignoredRequest, metrics, ruleScore, ruleGrade) ->
                Optional.empty()
        );

        OfferAgentEvaluationResponse response = service.evaluate(request);

        assertThat(response.getEvaluationMode()).isEqualTo("rule_only_fallback");
        assertThat(response.getOverallScore()).isEqualTo(response.getRuleScore());
        assertThat(response.getLlmJudgeScore()).isNull();
        assertThat(response.getLlmJudge().getAvailable()).isFalse();
        assertThat(response.getRisks()).isNotEmpty();
    }

    @Test
    void evaluateReturnsHighRuleScoreWhenRagToolsAndAnswerAreGrounded() {
        OfferAgentEvaluationResponse response = ruleOnlyService().evaluate(strongRequest());

        assertThat(response.getOverallScore()).isGreaterThanOrEqualTo(80);
        assertThat(response.getGrade()).isIn("A", "B");
        assertThat(response.getMetrics()).hasSize(4);
        assertThat(response.getStrengths()).isNotEmpty();
    }

    @Test
    void evaluateExposesRisksWhenNoToolsAndAnswerIsVague() {
        OfferAgentEvaluationResponse response = ruleOnlyService().evaluate(weakRequest());

        assertThat(response.getOverallScore()).isLessThan(60);
        assertThat(response.getGrade()).isEqualTo("E");
        assertThat(response.getRisks()).isNotEmpty();
        assertThat(response.getSuggestions()).contains("检查工具规划是否覆盖简历、岗位匹配和知识检索。");
    }

    private OfferAgentEvaluationService ruleOnlyService() {
        return new OfferAgentEvaluationService((request, metrics, ruleScore, ruleGrade) -> Optional.empty());
    }

    private OfferAgentEvaluationRequest strongRequest() {
        OfferAgentEvaluationRequest request = new OfferAgentEvaluationRequest();
        request.setUserId(7);
        request.setConversationId("c1");
        request.setMessage("分析我适合投哪些前端岗位");
        request.setTargetPosition("前端开发实习生");
        request.setFinalAnswer("""
                ## 结论
                你当前更适合前端开发实习生，简历中有 Vue 项目基础。
                ## 依据
                RAG 检索到了前端岗位知识，工具也给出了岗位匹配结果。
                ## 建议
                下一步补充 React、TypeScript、Vite 和接口联调证据，并按计划准备投递。
                """);
        request.setToolReport(new OfferAgentToolExecutionReport(
                "llm",
                null,
                List.of("tool_plan_source=llm"),
                List.of(
                        tool("get_resume_summary", "{\"detectedSkills\":[\"Vue\"]}", true),
                        tool("retrieve_knowledge", "{\"hitCount\":4,\"sources\":[{\"title\":\"前端岗位模型\"}]}", true),
                        tool("calculate_job_match_score", "{\"score\":78}", true)
                )
        ));
        return request;
    }

    private OfferAgentEvaluationRequest weakRequest() {
        OfferAgentEvaluationRequest request = new OfferAgentEvaluationRequest();
        request.setUserId(7);
        request.setMessage("帮我看看");
        request.setFinalAnswer("继续努力，保持学习。");
        request.setToolReport(new OfferAgentToolExecutionReport());
        return request;
    }

    private OfferAgentToolResult tool(String toolName, String outputJson, boolean success) {
        return new OfferAgentToolResult(
                toolName,
                "test tool",
                "{}",
                outputJson,
                toolName + " executed",
                success,
                null,
                "llm"
        );
    }
}
