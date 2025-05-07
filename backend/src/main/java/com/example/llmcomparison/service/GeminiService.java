package com.example.llmcomparison.service;

import com.google.cloud.vertexai.VertexAI;                                   // core client
import com.google.cloud.vertexai.generativeai.GenerativeModel;              // text‐generation
import com.google.cloud.vertexai.generativeai.ContentMaker;                 // helper to wrap text
import com.google.cloud.vertexai.generativeai.ResponseHandler;              // helper to extract response text
import com.google.cloud.vertexai.api.GenerateContentResponse;               // the SDK’s response type

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.InputStream;
import java.util.*;

@Service
public class GeminiService {
  private final GenerativeModel textModel;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final String imageEndpoint;
  private final String apiKey;
  private final String endpoint; // ✅ Added for scoreImage()
  private final RestTemplate restTemplate = new RestTemplate();

  public GeminiService(
      @Value("${vertex.project}") String project,
      @Value("${vertex.location}") String location,
      @Value("${vertex.textModel}") String textModelId,
      @Value("${google.api.key}") String apiKey) {

    // 1) Initialize VertexAI
    VertexAI vertexAi = new VertexAI(project, location);

    // 2) Construct the GenerativeModel
    this.textModel = new GenerativeModel(textModelId, vertexAi);

    // 3) Prepare the REST endpoints
    this.apiKey = apiKey;
    this.imageEndpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=" + apiKey;
    this.endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
  }

  public String generateText(String prompt) throws IOException {
    GenerateContentResponse resp = textModel.generateContent(ContentMaker.fromString(prompt));
    return ResponseHandler.getText(resp);
  }

  public String generateImage(String prompt) throws IOException, InterruptedException {
    String bodyJson = String.format(
      "{ \"instances\": [ { \"prompt\": \"%s\" } ] }",
      prompt.replace("\"", "\\\"")
    );
    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(imageEndpoint))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
        .build();

    HttpResponse<String> r = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    JSONObject json = new JSONObject(r.body());
    String b64 = json
      .getJSONArray("predictions")
      .getJSONObject(0)
      .getString("bytesBase64Encoded");

    return "data:image/png;base64," + b64;
  }

  public String analyzeImage(String base64Data, String instruction)
      throws IOException, InterruptedException {

    String url = String.format(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=%s",
      apiKey
    );

    if (base64Data.startsWith("data:image")) {
      base64Data = base64Data.replaceFirst("^data:image/[^;]+;base64,", "");
    }

    String bodyJson = """
      {
        "contents": [
          {
            "inlineData": {
              "mimeType": "image/png",
              "data": "%s"
            }
          },
          {
            "text": "%s"
          }
        ]
      }
      """
      .formatted(base64Data, instruction.replace("\"", "\\\""));

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
        .build();

    HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    if (res.statusCode() != 200) {
      throw new IOException("Gemini analyzeImage failed: " + res.body());
    }

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(res.body());
    return root.at("/choices/0/content/text").asText().trim();
  }

  public String scoreImage(String imageRef) throws Exception {
    byte[] bytes;
    if (imageRef.startsWith("data:")) {
      String b64 = imageRef.substring(imageRef.indexOf(',') + 1);
      bytes = Base64.getDecoder().decode(b64);
    } else {
      try (InputStream in = new URL(imageRef).openStream()) {
        bytes = in.readAllBytes();
      }
    }

    Map<String,Object> inlineData = Map.of(
      "inline_data", Map.of(
        "mime_type", detectMimeType(imageRef),
        "data", Base64.getEncoder().encodeToString(bytes)
      )
    );
    Map<String,Object> textPart = Map.of(
      "text", "Rate this image 0–10 (just give me the number)."
    );
    Map<String,Object> content = Map.of(
      "parts", List.of(inlineData, textPart)
    );
    Map<String,Object> body = Map.of("contents", List.of(content));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, headers);

    ResponseEntity<JsonNode> resp = restTemplate.exchange(endpoint, HttpMethod.POST, req, JsonNode.class);
    JsonNode bodyNode = resp.getBody();

    if (bodyNode == null || !bodyNode.has("candidates")) {
      throw new IOException("Gemini returned no candidates for image scoring.");
    }

    JsonNode candidates = bodyNode.get("candidates");
    if (candidates == null || !candidates.isArray() || candidates.isEmpty()) {
      throw new IOException("No candidates returned for image scoring: " + bodyNode.toPrettyString());
    }

    JsonNode parts = candidates.get(0).path("content").path("parts");
    if (parts == null || !parts.isArray() || parts.isEmpty()) {
      throw new IOException("No parts in response: " + bodyNode.toPrettyString());
    }

    return parts.get(0).path("text").asText().trim();
  }

  private String detectMimeType(String ref) {
    if (ref.startsWith("data:")) {
      return ref.substring(5, ref.indexOf(';'));
    }
    if (ref.endsWith(".webp")) return "image/webp";
    if (ref.endsWith(".png")) return "image/png";
    return "image/jpeg";
  }
}
