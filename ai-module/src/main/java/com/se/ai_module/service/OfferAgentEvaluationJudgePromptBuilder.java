package com.se.ai_module.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se.ai_module.dto.OfferAgentLlmJudgeRequest;
import org.springframework.stereotype.Component;

@Component
public class OfferAgentEvaluationJudgePromptBuilder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String build(OfferAgentLlmJudgeRequest request) {
        return """
                你是 OfferAgent 的评测裁判，负责从语义质量和业务有效性角度评估一轮智能求职助手回答。

                评测目标：
                1. 判断最终回答是否真的结合了用户问题、目标岗位、RAG 检索和工具结果。
                2. 判断建议是否具体、可执行、适合求职场景。
                3. 找出规则评测不容易发现的问题，例如泛泛而谈、证据与结论不匹配、建议顺序混乱。

                评分要求：
                - score 为 0-100 的整数。
                - grade 根据 score 给出：90-100 A，80-89 B，70-79 C，60-69 D，0-59 E。
                - 不要因为文风漂亮就给高分，必须看是否有证据、是否贴合目标岗位、是否能指导用户行动。
                - 如果工具或 RAG 结果很弱，但最终回答假装很确定，需要扣分。
                - 如果最终回答引用了工具结果并给出清晰行动方案，可以加分。

                只允许返回 JSON，不要返回 Markdown，不要解释 JSON 外的内容。格式如下：
                {
                  "score": 82,
                  "grade": "B",
                  "summary": "一句话总结本轮回答质量",
                  "strengths": ["优点1", "优点2"],
                  "risks": ["风险1", "风险2"],
                  "suggestions": ["建议1", "建议2"]
                }

                待评测数据：
                %s
                """.formatted(toJson(request));
    }

    private String toJson(OfferAgentLlmJudgeRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
