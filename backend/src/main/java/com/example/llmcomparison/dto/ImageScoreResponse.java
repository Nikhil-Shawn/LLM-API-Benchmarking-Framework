package com.example.llmcomparison.dto;

import java.util.Map;

public class ImageScoreResponse {
    private Map<String, Double> scores;
    private String winner;

    public ImageScoreResponse() {}

    public ImageScoreResponse(Map<String, Double> scores, String winner) {
        this.scores = scores;
        this.winner = winner;
    }

    public Map<String, Double> getScores() { return scores; }
    public String getWinner()       { return winner; }
}
