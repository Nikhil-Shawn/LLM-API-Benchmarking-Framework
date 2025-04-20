package com.example.llmcomparison.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AIScoringService {

    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");
    private static final String SCORING_ENDPOINT =
      "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro-002:generateContent?key="
      + API_KEY;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public String scoreImageWithLLM(String prompt, String base64Image) {
        try {
            // 1) Build the instruction prompt
            String formattedPrompt = String.format(
                "Evaluate this image against the prompt '%s'. "
              + "Score 1–10 for relevance, quality, creativity, consistency, composition. "
              + "Reply *only* with JSON: "
              + "{\"relevance\":number,\"quality\":number,\"creativity\":number,"
              + "\"consistency\":number,\"composition\":number}",
                prompt
            );

            // strip off any leading data URI prefix
            String cleanImage = base64Image.replaceFirst("^data:image/[^;]+;base64,", "");

            // 2) Build the payload WITHOUT any generationConfig
            String requestBody = mapper.writeValueAsString(Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", formattedPrompt),
                            Map.of("inline_data", Map.of(
                                "mime_type", "image/png",
                                "data", cleanImage
                            ))
                        )
                    )
                )
            ));

            // 3) Send it
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(SCORING_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("[AI Scoring] " + resp.body());

            // 4) Pull out the JSON string
            JsonNode root = mapper.readTree(resp.body());
            JsonNode text = root.at("/candidates/0/content/parts/0/text");
            if (text.isMissingNode()) {
                throw new RuntimeException("No text node in LLM response");
            }
            return text.asText();

        } catch (Exception e) {
            e.printStackTrace();
            // fallback: return an empty error JSON
            return "{\"error\":\"" + e.getMessage().replace("\"","\\\"") + "\"}";
        }
    }
}
