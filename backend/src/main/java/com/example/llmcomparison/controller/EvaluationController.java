package com.example.llmcomparison.controller;

import com.example.llmcomparison.model.EvaluationRequest;
import com.example.llmcomparison.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/submit-evaluation")
    public ResponseEntity<String> submitEvaluation(@RequestBody EvaluationRequest request) {
        evaluationService.submit(request);
        return ResponseEntity.ok("Evaluation submitted successfully");
    }
}