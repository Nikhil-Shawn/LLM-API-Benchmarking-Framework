package com.example.llmcomparison.service;

import com.google.cloud.vertexai.VertexAI;                                   // core client
import com.google.cloud.vertexai.generativeai.GenerativeModel;                // text-generation
import com.google.cloud.vertexai.generativeai.ContentMaker;                   // helper to wrap text
import com.google.cloud.vertexai.generativeai.ResponseHandler;                // helper to extract response text
import com.google.cloud.vertexai.api.GenerateContentResponse;                 // the SDK’s response type

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;


@Service
public class GeminiService {
  private final GenerativeModel textModel;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final String imageEndpoint;

  public GeminiService(
      @Value("${vertex.project}") String project,
      @Value("${vertex.location}") String location,
      @Value("${vertex.textModel}") String textModelId,
      @Value("${google.api.key}") String apiKey) {

    // 1) Initialize VertexAI
    VertexAI vertexAi = new VertexAI(project, location);

    // 2) Construct the GenerativeModel
    this.textModel = new GenerativeModel(textModelId, vertexAi);

    // 3) Prepare the REST endpoint for image generation
    this.imageEndpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key="
        + apiKey;
  }

  public String generateText(String prompt) throws IOException {
    // Wrap your prompt, send the request...
    GenerateContentResponse resp =
        textModel.generateContent(ContentMaker.fromString(prompt));

    // ...then extract all of the generated text in one go:
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
        .POST(BodyPublishers.ofString(bodyJson))
        .build();

    HttpResponse<String> r = httpClient.send(req, BodyHandlers.ofString());
    var json = new org.json.JSONObject(r.body());
    String b64 = json
      .getJSONArray("predictions")
      .getJSONObject(0)
      .getString("bytesBase64Encoded");

    return "data:image/png;base64," + b64;
  }
}
