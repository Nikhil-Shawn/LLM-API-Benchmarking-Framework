package com.example.llmcomparison.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StarCoderService {

    // Use only the lightweight Salesforce/codegen-350M-mono model
    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/Salesforce/codegen-350M-mono";
    private static final String API_TOKEN = System.getenv("HUGGINGFACE_API_KEY");

    // Extended timeout to allow model cold-start and generation
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient   httpClient;
    private final ObjectMapper objectMapper;

    public StarCoderService() {
        if (API_TOKEN == null || API_TOKEN.isBlank()) {
            throw new IllegalStateException("Missing HUGGINGFACE_API_KEY environment variable");
        }
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String generateCode(String prompt) {
        return callModel(MODEL_URL, prompt);
    }

    private String callModel(String url, String prompt) {
        try {
            // Payload with wait_for_model to block until ready
            Map<String, Object> payload = Map.of(
                "inputs", prompt,
                "options", Map.of("wait_for_model", true)
            );
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println("[StarCoder] Sending request: " + requestBody);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            System.out.println("[StarCoder RAW RESPONSE] " + body);

            JsonNode root = objectMapper.readTree(body);
            if (root.has("error")) {
                return "Error from Hugging Face: " + root.get("error").asText();
            }
            if (root.isArray() && root.size() > 0 && root.get(0).has("generated_text")) {
                return root.get(0).get("generated_text").asText();
            }
            return "Error: Unexpected response format";

        } catch (java.net.http.HttpTimeoutException e) {
            System.out.println("[StarCoder] Request timed out after " + REQUEST_TIMEOUT);
            return "Error: Request timed out";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
