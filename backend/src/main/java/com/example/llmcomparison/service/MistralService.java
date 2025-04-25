package com.example.llmcomparison.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;
import com.example.llmcomparison.model.GenerationResponse.ModelResult;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MistralService {
    private final WebClient webClient;

    @Value("${mistral.api.key}")
    private String apiKey;

    private static final String CHAT_URL = "https://api.mistral.ai/v1/chat/completions";

    public MistralService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl(CHAT_URL).build();
    }

    public Mono<String> generateChatCompletion(String modelId, String prompt) {
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(Map.of(
                "model",      modelId,
                "temperature",0.2,
                "max_tokens", 1024,
                "messages", List.of(
                  Map.of(
                    "role",    "user",
                    "content", "Write code to solve the following task. Provide only the code without explanations: " + prompt
                  )
                )
            ))
            .retrieve()
            .bodyToMono(JsonNode.class)
            .map(json ->
                json.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
            );
    }

    public String generateCode(String prompt) {
        return generateChatCompletion("mistral-small-latest", prompt).block();
    }

    public Mono<GenerationResponse> generate(GenerationRequest request) {
        return Flux.fromIterable(request.getModels())
            .flatMap(modelId ->
                generateChatCompletion(modelId, request.getPrompt())
                  .map(output -> {
                      ModelResult r = new ModelResult();
                      r.setModel(modelId);
                      r.setOutput(output);
                      return r;
                  })
            )
            .collectList()
            .map(resultsList -> {
                GenerationResponse resp = new GenerationResponse();
                resp.setResults(resultsList);
                return resp;
            });
    }
}
