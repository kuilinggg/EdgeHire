package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.OfferAgentToolContext;
import com.se.EdgeHire.DTO.OfferAgentToolParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
        return "\u4e3a\u76ee\u6807\u5c97\u4f4d\u751f\u6210\u786e\u5b9a\u6027\u7684\u4e2d\u6587\u9762\u8bd5\u51c6\u5907\u8ba1\u5212\u3002";
    }

    @Override
    public List<OfferAgentToolParameter> parameters() {
        return List.of(
                new OfferAgentToolParameter("targetPosition", "string", "\u76ee\u6807\u5c97\u4f4d\u540d\u79f0\u3002", false),
                new OfferAgentToolParameter("days", "integer", "\u51c6\u5907\u8ba1\u5212\u5929\u6570\u3002", false)
        );
    }

    @Override
    public Map<String, Object> execute(OfferAgentToolContext context) {
        String targetRole = String.valueOf(context.getArguments().getOrDefault("targetPosition", "AI Agent \u5b9e\u4e60\u751f"));
        RoleInterviewProfile profile = roleInterviewProfile(targetRole);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("targetRole", targetRole);
        output.put("roleCategory", profile.category());
        output.put("focusAreas", profile.focusAreas());
        output.put("questions", profile.questions());
        output.put("sevenDayPlan", profile.sevenDayPlan());
        output.put("summary", "\u5df2\u751f\u6210\u300c" + profile.category() + "\u300d\u65b9\u5411\u7684\u4e2d\u6587\u9762\u8bd5\u51c6\u5907\u8ba1\u5212");
        return output;
    }

    private RoleInterviewProfile roleInterviewProfile(String targetRole) {
        String text = targetRole.toLowerCase(Locale.ROOT);
        if (containsAny(text, "\u524d\u7aef", "frontend", "vue", "react")) {
            return new RoleInterviewProfile(
                    "\u524d\u7aef\u5f00\u53d1",
                    List.of("Vue/React \u7ec4\u4ef6\u8bbe\u8ba1", "\u8def\u7531\u4e0e\u72b6\u6001\u7ba1\u7406", "\u63a5\u53e3\u8054\u8c03\u4e0e\u5f02\u5e38\u53cd\u9988", "\u54cd\u5e94\u5f0f UI \u4e0e\u6784\u5efa\u4f18\u5316"),
                    List.of(
                            "\u8bf7\u4ecb\u7ecd\u4f60\u5982\u4f55\u8bbe\u8ba1 OfferAgent \u4e09\u4e2a\u5de5\u4f5c\u6d41\u9875\u9762\u7684\u7ec4\u4ef6\u590d\u7528\u3002",
                            "Vue \u9875\u9762\u4e2d\u5982\u4f55\u5904\u7406\u52a0\u8f7d\u6001\u3001\u7a7a\u72b6\u6001\u548c\u63a5\u53e3\u9519\u8bef\uff1f",
                            "\u5982\u679c\u8981\u8ba9\u65b0\u9875\u9762\u548c\u65e7 UI \u4fdd\u6301\u4e00\u81f4\uff0c\u4f60\u4f1a\u68c0\u67e5\u54ea\u4e9b\u5730\u65b9\uff1f",
                            "\u4f60\u662f\u5982\u4f55\u5728\u524d\u7aef\u5c55\u793a Agent \u6b65\u9aa4\u3001\u5de5\u5177\u8c03\u7528\u548c\u6700\u7ec8\u62a5\u544a\u7684\uff1f",
                            "\u5982\u679c\u63a5\u53e3\u8fd4\u56de\u7684 Markdown \u5f88\u957f\uff0c\u524d\u7aef\u5982\u4f55\u4fdd\u8bc1\u53ef\u8bfb\u6027\uff1f"
                    ),
                    List.of(
                            "\u7b2c 1 \u5929\uff1a\u68b3\u7406 Vue \u8def\u7531\u3001\u9875\u9762\u7ed3\u6784\u548c Element Plus \u7ec4\u4ef6\u4f7f\u7528\u3002",
                            "\u7b2c 2 \u5929\uff1a\u590d\u76d8 OfferAgent \u5de5\u4f5c\u6d41\u9875\u7684\u7ec4\u4ef6\u62bd\u8c61\u548c\u72b6\u6001\u7ba1\u7406\u3002",
                            "\u7b2c 3 \u5929\uff1a\u51c6\u5907\u63a5\u53e3\u8054\u8c03\u3001\u9519\u8bef\u63d0\u793a\u3001\u52a0\u8f7d\u6001\u548c\u7a7a\u72b6\u6001\u6848\u4f8b\u3002",
                            "\u7b2c 4 \u5929\uff1a\u8865\u5145 CSS \u5e03\u5c40\u3001\u54cd\u5e94\u5f0f\u548c\u6587\u672c\u6e32\u67d3\u7ec6\u8282\u3002",
                            "\u7b2c 5 \u5929\uff1a\u51c6\u5907\u4e00\u4e2a\u9875\u9762\u62c6\u5206\u548c\u7ec4\u4ef6\u590d\u7528\u7684\u6df1\u6316\u56de\u7b54\u3002",
                            "\u7b2c 6 \u5929\uff1a\u8865\u5145 build \u9a8c\u8bc1\u3001\u8def\u7531\u9a8c\u8bc1\u548c UI \u4e00\u81f4\u6027\u68c0\u67e5\u3002",
                            "\u7b2c 7 \u5929\uff1a\u6f14\u7ec3 1 \u5206\u949f\u524d\u7aef\u9879\u76ee\u4ecb\u7ecd\u548c 3 \u5206\u949f\u9875\u9762\u5b9e\u73b0\u8bb2\u89e3\u3002"
                    )
            );
        }
        if (containsAny(text, "\u540e\u7aef", "backend", "java", "spring")) {
            return new RoleInterviewProfile(
                    "Java \u540e\u7aef\u5f00\u53d1",
                    List.of("Spring Boot \u5206\u5c42\u8bbe\u8ba1", "JPA/MySQL \u6570\u636e\u8bbf\u95ee", "Tool Calling \u767d\u540d\u5355\u4e0e\u53c2\u6570\u6821\u9a8c", "JUnit \u6d4b\u8bd5\u4e0e fallback"),
                    List.of(
                            "\u8bf7\u4ecb\u7ecd OfferAgent \u540e\u7aef\u7684 Controller-Service-Repository \u8c03\u7528\u94fe\u3002",
                            "\u4e3a\u4ec0\u4e48 Tool Calling \u5fc5\u987b\u5728\u540e\u7aef\u505a\u5de5\u5177\u767d\u540d\u5355\u548c\u53c2\u6570\u6821\u9a8c\uff1f",
                            "\u4f60\u5982\u4f55\u8bbe\u8ba1\u591a Agent \u5de5\u4f5c\u6d41\u63a5\u53e3\u7684\u8fd4\u56de\u7ed3\u6784\uff1f",
                            "\u5982\u679c ai-module \u4e0d\u53ef\u7528\uff0c\u4e3b\u6d41\u7a0b\u5982\u4f55\u964d\u7ea7\uff1f",
                            "\u4f60\u5199\u4e86\u54ea\u4e9b\u6d4b\u8bd5\u6765\u9a8c\u8bc1 RAG\u3001Tool Calling \u548c\u5de5\u4f5c\u6d41\uff1f"
                    ),
                    List.of(
                            "\u7b2c 1 \u5929\uff1a\u68b3\u7406 Controller\u3001DTO\u3001Service\u3001Repository \u5206\u5c42\u804c\u8d23\u3002",
                            "\u7b2c 2 \u5929\uff1a\u590d\u76d8 RAG \u77e5\u8bc6\u8868\u3001\u68c0\u7d22\u670d\u52a1\u548c VectorStore \u964d\u7ea7\u903b\u8f91\u3002",
                            "\u7b2c 3 \u5929\uff1a\u51c6\u5907 Tool Calling \u6ce8\u518c\u3001\u89c4\u5212\u3001\u6821\u9a8c\u548c\u65e5\u5fd7\u8ffd\u8e2a\u8bb2\u89e3\u3002",
                            "\u7b2c 4 \u5929\uff1a\u68b3\u7406 workflow response \u7ed3\u6784\u548c\u524d\u7aef\u6d88\u8d39\u65b9\u5f0f\u3002",
                            "\u7b2c 5 \u5929\uff1a\u51c6\u5907\u5f02\u5e38\u5206\u652f\u3001fallback \u548c\u8fb9\u754c\u53c2\u6570\u95ee\u9898\u3002",
                            "\u7b2c 6 \u5929\uff1a\u590d\u76d8 Maven \u6d4b\u8bd5\u7528\u4f8b\u548c\u5173\u952e\u65ad\u8a00\u3002",
                            "\u7b2c 7 \u5929\uff1a\u6f14\u7ec3\u4ece\u8bf7\u6c42\u5230\u62a5\u544a\u8fd4\u56de\u7684\u5168\u94fe\u8def\u8bb2\u89e3\u3002"
                    )
            );
        }
        if (containsAny(text, "\u6570\u636e", "\u7b97\u6cd5", "\u673a\u5668\u5b66\u4e60", "ml", "python", "\u5206\u6790")) {
            return new RoleInterviewProfile(
                    "\u6570\u636e/\u7b97\u6cd5",
                    List.of("\u6570\u636e\u6e05\u6d17\u4e0e SQL", "Embedding \u8868\u793a", "\u53ec\u56de\u7387\u4e0e\u76f8\u4f3c\u5ea6\u8bc4\u4f30", "\u5b9e\u9a8c\u4e0e\u6307\u6807\u590d\u76d8"),
                    List.of(
                            "\u4f60\u4f1a\u5982\u4f55\u8bc4\u4f30 RAG \u68c0\u7d22\u7ed3\u679c\u7684\u8d28\u91cf\uff1f",
                            "Embedding \u5411\u91cf\u68c0\u7d22\u548c\u5173\u952e\u8bcd\u68c0\u7d22\u5206\u522b\u9002\u5408\u4ec0\u4e48\u573a\u666f\uff1f",
                            "\u5982\u679c\u7528\u6237\u53cd\u9988\u68c0\u7d22\u7ed3\u679c\u4e0d\u76f8\u5173\uff0c\u4f60\u4f1a\u4ece\u54ea\u4e9b\u6570\u636e\u6307\u6807\u6392\u67e5\uff1f",
                            "\u8bf7\u4ecb\u7ecd\u4e00\u4e2a\u4f60\u505a\u8fc7\u7684\u6570\u636e\u6e05\u6d17\u6216\u7279\u5f81\u8bbe\u8ba1\u8fc7\u7a0b\u3002",
                            "\u5982\u4f55\u8bbe\u8ba1\u4e00\u4e2a\u68c0\u7d22\u6548\u679c\u7684 A/B \u5206\u6790\uff1f"
                    ),
                    List.of(
                            "\u7b2c 1 \u5929\uff1a\u68b3\u7406 SQL\u3001\u6570\u636e\u6e05\u6d17\u548c\u6307\u6807\u5b9a\u4e49\u7ecf\u9a8c\u3002",
                            "\u7b2c 2 \u5929\uff1a\u590d\u76d8 Embedding\u3001\u76f8\u4f3c\u5ea6\u3001Top-K \u548c\u6df7\u5408\u68c0\u7d22\u7b56\u7565\u3002",
                            "\u7b2c 3 \u5929\uff1a\u51c6\u5907\u53ec\u56de\u7387\u3001\u547d\u4e2d\u7387\u3001\u8986\u76d6\u7387\u7b49\u8bc4\u4f30\u6307\u6807\u3002",
                            "\u7b2c 4 \u5929\uff1a\u5c06 OfferAgent RAG \u94fe\u8def\u6539\u5199\u6210\u6570\u636e\u6307\u6807\u89c6\u89d2\u7684\u9879\u76ee\u8868\u8fbe\u3002",
                            "\u7b2c 5 \u5929\uff1a\u51c6\u5907\u5931\u8d25\u6848\u4f8b\uff0c\u8bb2\u6e05\u5982\u4f55\u901a\u8fc7\u6570\u636e\u5b9a\u4f4d\u95ee\u9898\u3002",
                            "\u7b2c 6 \u5929\uff1a\u8865\u5145 Python/SQL \u9898\u548c\u5e38\u89c1\u7edf\u8ba1\u6307\u6807\u9898\u3002",
                            "\u7b2c 7 \u5929\uff1a\u6f14\u7ec3 RAG \u68c0\u7d22\u8d28\u91cf\u8bc4\u4f30\u7684\u5b8c\u6574\u56de\u7b54\u3002"
                    )
            );
        }
        if (containsAny(text, "\u4ea7\u54c1", "pm", "\u8fd0\u8425")) {
            return new RoleInterviewProfile(
                    "\u4ea7\u54c1/\u8fd0\u8425",
                    List.of("\u7528\u6237\u9700\u6c42\u4e0e\u75db\u70b9", "\u4e1a\u52a1\u6d41\u7a0b\u62c6\u89e3", "\u4f18\u5148\u7ea7\u5224\u65ad", "\u6570\u636e\u6307\u6807\u4e0e\u53cd\u9988\u95ed\u73af"),
                    List.of(
                            "\u4f60\u4e3a\u4ec0\u4e48\u628a OfferAgent \u62c6\u6210\u51b2\u523a\u89c4\u5212\u3001\u7b80\u5386\u4f18\u5316\u548c\u6a21\u62df\u9762\u8bd5\u4e09\u6761\u5de5\u4f5c\u6d41\uff1f",
                            "\u8fd9\u4e2a\u529f\u80fd\u7684\u6838\u5fc3\u7528\u6237\u8def\u5f84\u662f\u4ec0\u4e48\uff1f",
                            "\u4f60\u4f1a\u7528\u54ea\u4e9b\u6307\u6807\u8861\u91cf\u6c42\u804c\u52a9\u624b\u662f\u5426\u6709\u6548\uff1f",
                            "\u5982\u679c\u53ea\u80fd\u4fdd\u7559\u4e00\u4e2a\u5de5\u4f5c\u6d41\uff0c\u4f60\u4f1a\u4f18\u5148\u4fdd\u7559\u54ea\u4e2a\uff1f\u4e3a\u4ec0\u4e48\uff1f",
                            "\u5982\u4f55\u6839\u636e\u7528\u6237\u53cd\u9988\u8fed\u4ee3\u8fd9\u4e2a\u6c42\u804c\u8f85\u5bfc\u529f\u80fd\uff1f"
                    ),
                    List.of(
                            "\u7b2c 1 \u5929\uff1a\u68b3\u7406\u6c42\u804c\u8f85\u5bfc\u5e73\u53f0\u7684\u76ee\u6807\u7528\u6237\u548c\u6838\u5fc3\u75db\u70b9\u3002",
                            "\u7b2c 2 \u5929\uff1a\u590d\u76d8 OfferAgent \u56db\u4e2a\u5165\u53e3\u7684\u4e1a\u52a1\u8fb9\u754c\u548c\u8def\u5f84\u8bbe\u8ba1\u3002",
                            "\u7b2c 3 \u5929\uff1a\u51c6\u5907\u9700\u6c42\u4f18\u5148\u7ea7\u3001MVP \u548c\u8fed\u4ee3\u8282\u594f\u56de\u7b54\u3002",
                            "\u7b2c 4 \u5929\uff1a\u8865\u5145\u6570\u636e\u6307\u6807\uff0c\u5982\u4f7f\u7528\u7387\u3001\u5b8c\u6210\u7387\u3001\u7528\u6237\u8bc4\u5206\u548c\u8f6c\u5316\u7387\u3002",
                            "\u7b2c 5 \u5929\uff1a\u51c6\u5907\u4e00\u4e2a\u4ece\u95ee\u9898\u5230\u529f\u80fd\u7684\u4ea7\u54c1\u51b3\u7b56\u6848\u4f8b\u3002",
                            "\u7b2c 6 \u5929\uff1a\u590d\u76d8\u7ade\u54c1\u548c\u540e\u7eed\u5546\u4e1a\u5316\u53ef\u80fd\u6027\u3002",
                            "\u7b2c 7 \u5929\uff1a\u6f14\u7ec3 3 \u5206\u949f\u4ea7\u54c1\u5316\u9879\u76ee\u8bb2\u89e3\u3002"
                    )
            );
        }
        return new RoleInterviewProfile(
                "AI Agent \u5e94\u7528\u5f00\u53d1",
                List.of("RAG \u68c0\u7d22\u94fe\u8def", "Tool Calling \u5b89\u5168\u8fb9\u754c", "\u591a Agent \u5de5\u4f5c\u6d41\u8bbe\u8ba1", "Spring Boot \u5de5\u7a0b\u96c6\u6210"),
                List.of(
                        "\u4e3a\u4ec0\u4e48 OfferAgent \u9700\u8981 RAG\uff0c\u800c\u4e0d\u662f\u76f4\u63a5\u628a\u7528\u6237\u95ee\u9898\u4ea4\u7ed9\u5927\u6a21\u578b\uff1f",
                        "Tool Calling \u5982\u4f55\u6620\u5c04\u5230\u786e\u5b9a\u6027\u7684\u4e1a\u52a1\u63a5\u53e3\uff0c\u5e76\u907f\u514d\u6a21\u578b\u8d8a\u6743\u8c03\u7528\uff1f",
                        "\u5173\u952e\u8bcd\u68c0\u7d22\u548c\u5411\u91cf\u68c0\u7d22\u5206\u522b\u89e3\u51b3\u4ec0\u4e48\u95ee\u9898\uff1f\u4e3a\u4ec0\u4e48\u8981\u505a\u6df7\u5408\u68c0\u7d22\uff1f",
                        "\u5982\u679c RAG \u6ca1\u6709\u547d\u4e2d\u76f8\u5173\u77e5\u8bc6\u7247\u6bb5\uff0c\u7cfb\u7edf\u5e94\u8be5\u5982\u4f55\u964d\u7ea7\uff1f",
                        "\u591a Agent \u5de5\u4f5c\u6d41\u4e2d\u6bcf\u4e2a Agent \u7684\u8fb9\u754c\u662f\u4ec0\u4e48\uff1f\u4e3a\u4ec0\u4e48\u8fd9\u6837\u62c6\u5206\uff1f"
                ),
                List.of(
                        "\u7b2c 1 \u5929\uff1a\u68b3\u7406\u9879\u76ee\u67b6\u6784\u3001\u6570\u636e\u5e93\u8868\u548c\u6838\u5fc3\u6570\u636e\u6d41\u3002",
                        "\u7b2c 2 \u5929\uff1a\u590d\u76d8 RAG \u7684\u6587\u6863\u5207\u5206\u3001\u53ec\u56de\u3001\u91cd\u6392\u548c\u964d\u7ea7\u7b56\u7565\u3002",
                        "\u7b2c 3 \u5929\uff1a\u51c6\u5907 Tool Calling \u7684\u5de5\u5177\u6ce8\u518c\u3001\u53c2\u6570\u6821\u9a8c\u3001\u65e5\u5fd7\u8ffd\u8e2a\u548c fallback \u673a\u5236\u3002",
                        "\u7b2c 4 \u5929\uff1a\u51c6\u5907 Spring Boot\u3001JPA\u3001WebClient \u7b49\u5de5\u7a0b\u5b9e\u73b0\u95ee\u9898\u3002",
                        "\u7b2c 5 \u5929\uff1a\u56f4\u7ed5\u591a Agent \u5de5\u4f5c\u6d41\u51c6\u5907\u9879\u76ee\u6df1\u6316\u95ee\u7b54\u3002",
                        "\u7b2c 6 \u5929\uff1a\u4f18\u5316\u7b80\u5386\u4e2d\u7684\u9879\u76ee\u63cf\u8ff0\uff0c\u8865\u5145\u91cf\u5316\u7ed3\u679c\u548c\u5de5\u7a0b\u7ec6\u8282\u3002",
                        "\u7b2c 7 \u5929\uff1a\u5b8c\u6574\u6f14\u793a\u7cfb\u7edf\uff0c\u5e76\u7ec3\u4e60 1 \u5206\u949f\u548c 3 \u5206\u949f\u9879\u76ee\u4ecb\u7ecd\u3002"
                )
        );
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private record RoleInterviewProfile(
            String category,
            List<String> focusAreas,
            List<String> questions,
            List<String> sevenDayPlan) {
    }
}
