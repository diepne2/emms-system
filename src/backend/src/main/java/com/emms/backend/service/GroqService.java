package com.emms.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    public GroqService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String ask(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing Groq API key. Please configure groq.api.key.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You are a strict JSON-only maintenance decision support assistant."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(
                GROQ_URL,
                request,
                String.class
        );

        return extractContent(response);
    }

    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse Groq response", e);
        }
    }
}