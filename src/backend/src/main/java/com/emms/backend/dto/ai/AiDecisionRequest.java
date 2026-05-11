package com.emms.backend.dto.ai;

public class AiDecisionRequest {

    private String question;

    public AiDecisionRequest() {
    }

    public AiDecisionRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}