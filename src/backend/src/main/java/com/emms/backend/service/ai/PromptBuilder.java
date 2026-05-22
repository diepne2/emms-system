package com.emms.backend.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    private final ObjectMapper objectMapper;

    public PromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String build(Object riskData, String question, String period) {
        String riskJson;

        try {
            riskJson = objectMapper.writeValueAsString(riskData);
        } catch (Exception e) {
            riskJson = "[]";
        }

        return """
                You are an Industrial AI Decision Support System for an EMMS maintenance platform.

                Your task:
                - Analyze equipment risk data.
                - Explain the maintenance risk clearly.
                - Generate practical maintenance recommendations.
                - Generate priority actions for high-risk assets.

                Strict rules:
                - Use ONLY the provided risk data.
                - Do NOT invent assets, scores, work orders, downtime, or status.
                - If the data is insufficient, state that clearly.
                - Return ONLY valid JSON.
                - Do not wrap the result in markdown.
                - Do not add explanation outside JSON.

                Period: %s

                User question:
                %s

                Risk data:
                %s

                Required JSON format:
                {
                  "summary": "short Vietnamese summary",
                  "recommendations": [
                    "recommendation 1",
                    "recommendation 2"
                  ],
                  "priorityActions": [
                    "action 1",
                    "action 2"
                  ]
                }
                """.formatted(
                period,
                question == null || question.isBlank()
                        ? "Analyze current maintenance risk."
                        : question,
                riskJson
        );
    }
}