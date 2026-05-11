package com.emms.backend.dto.ai;

public class AiDecisionResponse {

    private String answer;

    public AiDecisionResponse() {
    }

    public AiDecisionResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}