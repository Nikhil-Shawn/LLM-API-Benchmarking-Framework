package com.example.llmcomparison.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.llmcomparison.service.StarCoderService;

@RestController
@RequestMapping("/api/starcoder")
@CrossOrigin(origins = "*")
public class StarCoderController {

    @Autowired
    private StarCoderService starCoderService;

    @PostMapping("/generate")
    public String generateCode(@RequestBody String prompt) {
        return starCoderService.generateCode(prompt);
    }
}
