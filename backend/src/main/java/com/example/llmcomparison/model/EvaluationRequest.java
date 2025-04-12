package com.example.llmcomparison.model;

import java.util.Map;

public class EvaluationRequest {
    private String useCase;
    private String prompt;
    private Map<String, String> models;
    private Map<String, Map<String, Integer>> scores;
    private String feedback;

    public String getUseCase() {
        return useCase;
    }

    public void setUseCase(String useCase) {
        this.useCase = useCase;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Map<String, String> getModels() {
        return models;
    }

    public void setModels(Map<String, String> models) {
        this.models = models;
    }

    public Map<String, Map<String, Integer>> getScores() {
        return scores;
    }

    public void setScores(Map<String, Map<String, Integer>> scores) {
        this.scores = scores;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}