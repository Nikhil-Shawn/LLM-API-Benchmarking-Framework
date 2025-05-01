package com.example.llmcomparison.controller;

import com.example.llmcomparison.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Simple DTO for binding {"prompt":"..."} from the request body.
     */
    public static class PromptRequest {
        private String prompt;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateText(@RequestBody PromptRequest request) throws IOException {
        String result = geminiService.generateText(request.getPrompt());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/image")
    public ResponseEntity<String> generateImage(@RequestBody PromptRequest request)
            throws IOException, InterruptedException {
        String result = geminiService.generateImage(request.getPrompt());
        return ResponseEntity.ok(result);
    }
}
