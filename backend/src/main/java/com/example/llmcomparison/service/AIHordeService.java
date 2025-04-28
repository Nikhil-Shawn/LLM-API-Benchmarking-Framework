package com.example.llmcomparison.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AIHordeService {

    private static final String API_KEY           = System.getenv("STABLE_HORDE_KEY");
    private static final String ASYNC_ENDPOINT    = "https://stablehorde.net/api/v2/generate/async";
    private static final String STATUS_ENDPOINT   = "https://stablehorde.net/api/v2/generate/status/";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient   client;
    private final ObjectMapper mapper;

    public AIHordeService() {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException("Missing STABLE_HORDE_KEY environment variable");
        }
        this.client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Generate an image asynchronously and then fetch the final result.
     */
    public String generateImage(String prompt) {
        try {
            // Submit job
            ObjectNode payload = mapper.createObjectNode();
            payload.put("prompt", prompt);
            ObjectNode params = mapper.createObjectNode();
            params.put("steps", 30);
            params.put("width", 512);
            params.put("height", 512);
            payload.set("params", params);
            payload.put("civitai", false);

            String reqJson = mapper.writeValueAsString(payload);
            System.out.println("[AIHorde] Async request: " + reqJson);

            HttpRequest asyncReq = HttpRequest.newBuilder()
                    .uri(URI.create(ASYNC_ENDPOINT))
                    .timeout(REQUEST_TIMEOUT)
                    .header("apikey", API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(reqJson))
                    .build();

            String asyncResp = client.send(asyncReq, HttpResponse.BodyHandlers.ofString()).body();
            System.out.println("[AIHorde] Async response: " + asyncResp);
            JsonNode asyncJson = mapper.readTree(asyncResp);
            String id = asyncJson.path("id").asText(null);
            if (id == null) return "Error: No job ID returned";

            // Poll status until done
            while (true) {
                Thread.sleep(2000);
                HttpRequest statusReq = HttpRequest.newBuilder()
                        .uri(URI.create(STATUS_ENDPOINT + id))
                        .timeout(REQUEST_TIMEOUT)
                        .header("apikey", API_KEY)
                        .build();

                String statusResp = client.send(statusReq, HttpResponse.BodyHandlers.ofString()).body();
                System.out.println("[AIHorde] Status response: " + statusResp);
                JsonNode statusJson = mapper.readTree(statusResp);

                if (statusJson.path("done").asBoolean(false)) {
                    // 'generations' now lives under the status call
                    JsonNode gens = statusJson.path("generations");
                    if (gens.isArray() && gens.size() > 0 && gens.get(0).has("img")) {
                        return gens.get(0).get("img").asText();
                    }
                    return "Error: No image URL in status response";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
