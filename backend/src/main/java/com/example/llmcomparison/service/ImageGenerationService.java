package com.example.llmcomparison.service;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageGenerationService {

    public GenerationResponse generate(GenerationRequest request) {
        GenerationResponse response = new GenerationResponse();
        List<GenerationResponse.ModelResult> results = new ArrayList<>();

        for (String model : request.getModels()) {
            String output = callModelAPI(model, request.getPrompt());
            GenerationResponse.ModelResult result = new GenerationResponse.ModelResult();
            result.setModel(model);
            result.setOutput(output);
            results.add(result);
        }

        response.setResults(results);
        return response;
    }

    private String callModelAPI(String modelId, String prompt) {
        return "[Generated image link for model " + modelId + " and prompt: " + prompt + "]";
    }
}