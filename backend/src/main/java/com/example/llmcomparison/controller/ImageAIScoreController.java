package com.example.llmcomparison.controller;

import com.example.llmcomparison.service.AIScoringService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/score")
public class ImageAIScoreController {

    @Autowired
    private AIScoringService aiScoringService;

    @PostMapping("/image")
    public Map<String, Integer> scoreImage(@RequestBody Map<String, String> request) {
        String prompt      = request.get("prompt");
        String base64Image = request.get("image");

        try {
            String aiResponse = aiScoringService.scoreImageWithLLM(prompt, base64Image);

            // parse the JSON text that the LLM returned
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(aiResponse);

            if (!json.has("relevance")) {
                throw new IllegalArgumentException("Missing score fields");
            }

            Map<String, Integer> scores = new HashMap<>();
            scores.put("relevance",  json.get("relevance").asInt());
            scores.put("quality",    json.get("quality").asInt());
            scores.put("creativity", json.get("creativity").asInt());
            scores.put("consistency",json.get("consistency").asInt());
            scores.put("composition",json.get("composition").asInt());
            return scores;

        } catch (Exception e) {
            e.printStackTrace();
            // on any failure, return zeros
            Map<String, Integer> fallback = Map.of(
                "relevance",  0,
                "quality",    0,
                "creativity", 0,
                "consistency",0,
                "composition",0
            );
            return fallback;
        }
    }
}
