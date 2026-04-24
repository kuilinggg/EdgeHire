package com.se.EdgeHire.Service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LocalHashEmbeddingService implements OfferAgentEmbeddingService {
    private static final int DIMENSION = 128;

    @Override
    public double[] embed(String text) {
        double[] vector = new double[DIMENSION];
        for (String token : tokenize(text)) {
            int index = positiveHash(token) % DIMENSION;
            vector[index] += weight(token);
        }
        normalize(vector);
        return vector;
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = normalizeTerms(text);
        String[] parts = normalized
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .split(" ");

        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].length() >= 2 && parts[i + 1].length() >= 2) {
                tokens.add(parts[i] + "_" + parts[i + 1]);
            }
        }
        return tokens;
    }

    private String normalizeTerms(String text) {
        String value = text.toLowerCase(Locale.ROOT);
        value = value.replace("tool calling", "toolcalling tool functioncall function");
        value = value.replace("function calling", "toolcalling tool functioncall function");
        value = value.replace("rag", "rag retrieval knowledgebase vector embedding");
        value = value.replace("ai agent", "aiagent agent llm workflow");
        value = value.replace("spring boot", "springboot java backend");
        value = value.replace("大模型", "llm aiagent agent");
        value = value.replace("工具调用", "toolcalling functioncall tool");
        value = value.replace("检索增强", "rag retrieval knowledgebase");
        value = value.replace("向量", "vector embedding");
        value = value.replace("知识库", "knowledgebase rag retrieval");
        value = value.replace("多agent", "multiagent agent workflow");
        value = value.replace("后端", "backend java springboot");
        value = value.replace("简历", "resume cv");
        value = value.replace("面试", "interview");
        return value;
    }

    private double weight(String token) {
        if (token.contains("aiagent") || token.contains("rag") || token.contains("toolcalling")
                || token.contains("springboot") || token.contains("embedding") || token.contains("vector")) {
            return 2.0;
        }
        return 1.0;
    }

    private int positiveHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            int value = 0;
            for (int i = 0; i < 4; i++) {
                value = (value << 8) | (bytes[i] & 0xff);
            }
            return value & 0x7fffffff;
        } catch (NoSuchAlgorithmException e) {
            return token.hashCode() & 0x7fffffff;
        }
    }

    private void normalize(double[] vector) {
        double sum = 0;
        for (double value : vector) {
            sum += value * value;
        }
        if (sum == 0) {
            return;
        }
        double norm = Math.sqrt(sum);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
