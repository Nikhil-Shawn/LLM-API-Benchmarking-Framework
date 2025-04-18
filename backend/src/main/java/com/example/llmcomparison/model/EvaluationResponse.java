package com.example.llmcomparison.model;

public class EvaluationResponse {
    private boolean success;
    private String message;
    private String highestScoringModel;
    private int highestScore;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getHighestScoringModel() {
        return highestScoringModel;
    }
    
    public void setHighestScoringModel(String highestScoringModel) {
        this.highestScoringModel = highestScoringModel;
    }
    
    public int getHighestScore() {
        return highestScore;
    }
    
    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }
}