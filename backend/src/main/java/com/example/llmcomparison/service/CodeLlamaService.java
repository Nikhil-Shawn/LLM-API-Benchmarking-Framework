package com.example.llmcomparison.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CodeLlamaService {

  private final String huggingFaceApiKey;
  private final String endpointUrl;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public CodeLlamaService(
      @Value("${huggingface.api.key}") String apiKey) {
    this.huggingFaceApiKey = apiKey;
    this.endpointUrl = "https://api-inference.huggingface.co/models/meta-llama/CodeLlama-7b-Instruct-hf";
  }

  public String generateCode(String prompt) throws IOException {
    String bodyJson = String.format("{\"inputs\": \"%s\"}", prompt.replace("\"", "\\\""));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(endpointUrl))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + huggingFaceApiKey)
        .POST(BodyPublishers.ofString(bodyJson))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
      JSONArray jsonArray = new JSONArray(response.body());

      if (jsonArray.length() > 0) {
        JSONObject result = jsonArray.getJSONObject(0);
        return result.getString("generated_text");
      } else {
        return "No output received from Code Llama.";
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "Request interrupted: " + e.getMessage();
    }
  }
}
