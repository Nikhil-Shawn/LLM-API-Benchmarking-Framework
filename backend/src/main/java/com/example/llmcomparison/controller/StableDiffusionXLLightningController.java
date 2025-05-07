package com.example.llmcomparison.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;
import com.example.llmcomparison.service.StableDiffusionXLLightningService;

@RestController
@RequestMapping("/api/sdxl")
@CrossOrigin(origins = "*")
public class StableDiffusionXLLightningController {

    @Autowired
    private StableDiffusionXLLightningService sdxlService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generateImage(
            @RequestBody GenerationRequest request) {
        return ResponseEntity.ok(sdxlService.generate(request));
    }
}
