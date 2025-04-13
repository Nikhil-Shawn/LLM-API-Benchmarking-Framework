package com.example.llmcomparison.service;

import com.example.llmcomparison.model.EvaluationRequest;
import org.springframework.stereotype.Service;

@Service
public class EvaluationService {

    public void submit(EvaluationRequest request) {
        System.out.println("Received Evaluation:");
        System.out.println("Use case: " + request.getUseCase());
        System.out.println("Prompt: " + request.getPrompt());
        System.out.println("Scores: " + request.getScores());
        System.out.println("Feedback: " + request.getFeedback());
    }
}