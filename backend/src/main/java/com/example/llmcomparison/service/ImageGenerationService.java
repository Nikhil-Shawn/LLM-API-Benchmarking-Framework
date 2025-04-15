package com.example.llmcomparison.service;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageGenerationService {

    public GenerationResponse generate(GenerationRequest request) {
        GenerationResponse response = new GenerationResponse();
        List<GenerationResponse.ModelResult> results = new ArrayList<>();

        // Log the generation request
        System.out.println("Image Generation Request:");
        System.out.println("Prompt: " + request.getPrompt());
        System.out.println("Models: " + request.getModels());

        for (String model : request.getModels()) {
            String output = generateImageUrlForModel(model, request.getPrompt());
            GenerationResponse.ModelResult result = new GenerationResponse.ModelResult();
            result.setModel(model);
            result.setOutput(output);
            results.add(result);
        }

        response.setResults(results);
        return response;
    }

    private String generateImageUrlForModel(String modelId, String prompt) {
        // Generate a unique ID for each image request
        String imageId = UUID.randomUUID().toString().substring(0, 8);
        
        // In a real implementation, these would be actual URLs to generated images
        // For this demo, we'll use placeholder URLs that represent where the images would be
        switch (modelId) {
            case "stablediffusion":
                return "https://api.example.com/images/stablediffusion/" + imageId + "?prompt=" + sanitizePrompt(prompt);
            case "dalle":
                return "https://api.example.com/images/dalle/" + imageId + "?prompt=" + sanitizePrompt(prompt);
            case "midjourney":
                return "https://api.example.com/images/midjourney/" + imageId + "?prompt=" + sanitizePrompt(prompt);
            default:
                return "https://api.example.com/images/placeholder";
        }
    }
    
    private String sanitizePrompt(String prompt) {
        // Replace spaces and special characters for URL
        return prompt.replaceAll("[\\s+]", "-").replaceAll("[^a-zA-Z0-9-]", "");
    }
}