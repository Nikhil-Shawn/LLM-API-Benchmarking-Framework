package com.example.llmcomparison.controller;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;
import com.example.llmcomparison.service.CodeGenerationService;
import com.example.llmcomparison.service.ImageGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GenerationController {

    @Autowired
    private CodeGenerationService codeService;

    @Autowired
    private ImageGenerationService imageService;

    @PostMapping("/generate-code")
    public ResponseEntity<GenerationResponse> generateCode(@RequestBody GenerationRequest request) {
        return ResponseEntity.ok(codeService.generate(request));
    }

    @PostMapping("/generate-image")
    public ResponseEntity<GenerationResponse> generateImage(@RequestBody GenerationRequest request) {
        return ResponseEntity.ok(imageService.generate(request));
    }
}