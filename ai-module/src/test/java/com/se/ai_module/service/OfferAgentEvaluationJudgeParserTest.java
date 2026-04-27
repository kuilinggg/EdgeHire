package com.se.ai_module.service;

import com.se.ai_module.dto.OfferAgentLlmJudgeResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OfferAgentEvaluationJudgeParserTest {

    @Test
    void parseExtractsJudgeJsonFromMarkdownWrappedContent() {
        OfferAgentEvaluationJudgeParser parser = new OfferAgentEvaluationJudgeParser();

        OfferAgentLlmJudgeResult result = parser.parse("""
                ```json
                {
                  "score": 86,
                  "grade": "B",
                  "summary": "回答结合了工具结果和岗位目标。",
                  "strengths": ["证据链清晰"],
                  "risks": ["缺少优先级"],
                  "suggestions": ["增加投递清单"]
                }
                ```
                """);

        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getScore()).isEqualTo(86);
        assertThat(result.getGrade()).isEqualTo("B");
        assertThat(result.getStrengths()).contains("证据链清晰");
        assertThat(result.getRisks()).contains("缺少优先级");
        assertThat(result.getSuggestions()).contains("增加投递清单");
    }

    @Test
    void parseReturnsUnavailableWhenContentIsNotJson() {
        OfferAgentEvaluationJudgeParser parser = new OfferAgentEvaluationJudgeParser();

        OfferAgentLlmJudgeResult result = parser.parse("not json");

        assertThat(result.getAvailable()).isFalse();
        assertThat(result.getFallbackReason()).contains("Failed to parse");
    }
}
