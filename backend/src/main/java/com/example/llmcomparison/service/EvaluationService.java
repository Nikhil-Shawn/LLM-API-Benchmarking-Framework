package com.example.llmcomparison.service;

import com.example.llmcomparison.model.EvaluationRequest;
import com.example.llmcomparison.model.EvaluationResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EvaluationService {

    public EvaluationResponse submit(EvaluationRequest request) {
        // Log the evaluation data
        System.out.println("Received Evaluation:");
        System.out.println("Use case: " + request.getUseCase());
        System.out.println("Prompt: " + request.getPrompt());
        System.out.println("Models: " + request.getModels());
        System.out.println("Scores: " + request.getScores());
        System.out.println("Feedback: " + request.getFeedback());
        
        // Calculate which model scored the highest
        Map<String, Integer> totalScores = new HashMap<>();
        
        // Calculate total scores for each model
        for (Map.Entry<String, Map<String, Integer>> modelEntry : request.getScores().entrySet()) {
            String model = modelEntry.getKey();
            Map<String, Integer> scores = modelEntry.getValue();
            
            int totalScore = scores.values().stream().mapToInt(Integer::intValue).sum();
            totalScores.put(model, totalScore);
        }
        
        // Find the highest scoring model
        String highestScoringModel = null;
        int highestScore = -1;
        
        for (Map.Entry<String, Integer> entry : totalScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                highestScoringModel = entry.getKey();
            }
        }
        
        // Get the model ID for the winner
        String winningModelId = null;
        if (highestScoringModel != null) {
            String modelKey = "model" + highestScoringModel; // Convert A, B, C to modelA, modelB, modelC
            winningModelId = request.getModels().get(modelKey);
        }
        
        // Create and return response
        EvaluationResponse response = new EvaluationResponse();
        response.setSuccess(true);
        response.setMessage("Evaluation submitted successfully");
        response.setHighestScoringModel(winningModelId);
        response.setHighestScore(highestScore);
        
        return response;
    }
}