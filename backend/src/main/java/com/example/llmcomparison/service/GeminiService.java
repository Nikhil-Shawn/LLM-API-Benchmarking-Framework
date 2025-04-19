package com.example.llmcomparison.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiService {

    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");
    private static final String CODE_ENDPOINT = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro-002:generateContent?key=" + API_KEY;
    private static final String IMAGE_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=" + API_KEY;

    public String generateCode(String prompt) {
        return callGeminiTextAPI(CODE_ENDPOINT, prompt);
    }

    public String generateImage(String prompt) {
        return callGeminiImageAPI(IMAGE_ENDPOINT, prompt);
    }

    private String callGeminiTextAPI(String endpoint, String prompt) {
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
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[Gemini Code JSON] " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            return root.at("/candidates/0/content/parts/0/text").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    private String callGeminiImageAPI(String endpoint, String prompt) {
      try {
          String requestBody = String.format("""
              {
                "instances": [
                  {
                    "prompt": "%s"
                  }
                ]
              }
              """, prompt.replace("\"", "\\\""));
  
          HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(endpoint))
                  .header("Content-Type", "application/json")
                  .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                  .build();
  
          HttpClient client = HttpClient.newHttpClient();
          HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
  
          System.out.println("[Gemini Image JSON] " + response.body());
  
          ObjectMapper mapper = new ObjectMapper();
          JsonNode root = mapper.readTree(response.body());
  
          JsonNode images = root.at("/predictions/0/bytesBase64Encoded");
          if (!images.isMissingNode()) {
              return "data:image/png;base64," + images.asText();
          }
  
          return "No image data returned.";
  
      } catch (Exception e) {
          e.printStackTrace();
          return "Error: " + e.getMessage();
      }
  }
  

    private String toJsonString(String text) {
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }
}
