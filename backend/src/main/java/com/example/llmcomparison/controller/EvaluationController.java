package com.example.llmcomparison.controller;

import com.example.llmcomparison.model.EvaluationRequest;
import com.example.llmcomparison.model.EvaluationResponse;
import com.example.llmcomparison.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/submit-evaluation")
    public ResponseEntity<EvaluationResponse> submitEvaluation(@RequestBody EvaluationRequest request) {
        EvaluationResponse response = evaluationService.submit(request);
        return ResponseEntity.ok(response);
    }
}