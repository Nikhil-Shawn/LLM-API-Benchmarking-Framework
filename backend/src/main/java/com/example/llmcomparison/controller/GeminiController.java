package com.example.llmcomparison.controller;

import com.example.llmcomparison.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/generate")
    public String generateCode(@RequestBody String prompt) {
        return geminiService.generateCode(prompt);
    }

    @PostMapping("/image")
    public String generateImage(@RequestBody String prompt) {
        return geminiService.generateImage(prompt);
    }
}