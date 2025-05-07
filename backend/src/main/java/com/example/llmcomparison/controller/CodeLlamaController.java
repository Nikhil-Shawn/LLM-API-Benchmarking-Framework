package com.example.llmcomparison.controller;

import com.example.llmcomparison.service.CodeLlamaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/code-llama")
public class CodeLlamaController {

    private final CodeLlamaService codeLlamaService;

    public CodeLlamaController(CodeLlamaService codeLlamaService) {
        this.codeLlamaService = codeLlamaService;
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
    public ResponseEntity<String> generateCode(@RequestBody PromptRequest request) throws IOException {
        String result = codeLlamaService.generateCode(request.getPrompt());
        return ResponseEntity.ok(result);
    }
}
