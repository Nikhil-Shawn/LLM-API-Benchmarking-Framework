package com.example.llmcomparison.model;

import java.util.List;

public class GenerationRequest {
    private String prompt;
    private List<String> models;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }
}