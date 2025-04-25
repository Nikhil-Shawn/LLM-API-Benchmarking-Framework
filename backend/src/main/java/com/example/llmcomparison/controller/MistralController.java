package com.example.llmcomparison.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.llmcomparison.service.MistralService;

@RestController
@RequestMapping("/api/mistral")
@CrossOrigin(origins = "*")
public class MistralController {

    @Autowired
    private MistralService mistralService;

    @PostMapping("/generate")
    public String generateCode(@RequestBody String prompt) {
        return mistralService.generateCode(prompt);
    }
}
