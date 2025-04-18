package com.example.llmcomparison.service;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiService {

    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro-002:generateContent?key=" + API_KEY;

    public String generateCode(String prompt) {
        try {
            String requestBody = String.format("""
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": %s
                    }
                  ]
                }
              ]
            }
            """, toJsonString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String toJsonString(String text) {
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }
}
