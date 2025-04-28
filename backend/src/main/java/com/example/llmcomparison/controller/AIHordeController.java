package com.example.llmcomparison.controller;

import com.example.llmcomparison.service.AIHordeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aihorde")
public class AIHordeController {

    @Autowired
    private AIHordeService aihordeService;

    @PostMapping("/image")
    public String generateImage(@RequestBody String prompt) {
        return aihordeService.generateImage(prompt);
    }
}
