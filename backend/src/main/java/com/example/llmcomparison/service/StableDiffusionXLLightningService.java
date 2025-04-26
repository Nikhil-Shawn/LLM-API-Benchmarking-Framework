package com.example.llmcomparison.service;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.llmcomparison.model.GenerationRequest;
import com.example.llmcomparison.model.GenerationResponse;

@Service
public class StableDiffusionXLLightningService {

    @Value("${cloudflare.account-id}")
    private String accountId;

    @Value("${cloudflare.api-token}")
    private String apiToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public GenerationResponse generate(GenerationRequest request) {

        String url = String.format(
            "https://api.cloudflare.com/client/v4/accounts/%s/ai/run/@cf/bytedance/stable-diffusion-xl-lightning",
            accountId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HttpEntity<?> entity = new HttpEntity<>(
            Collections.singletonMap("prompt", request.getPrompt()),
            headers
        );

        ResponseEntity<byte[]> cfResponse = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            byte[].class
        );

        byte[] imageBytes = cfResponse.getBody();
        MediaType contentType = cfResponse.getHeaders().getContentType();
        String mimeType = (contentType != null ? contentType.toString() : "image/png");

        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String dataUri = "data:" + mimeType + ";base64," + base64;

        GenerationResponse response = new GenerationResponse();
        GenerationResponse.ModelResult result = new GenerationResponse.ModelResult();
        result.setModel("stable-diffusion-xl-lightning");
        result.setOutput(dataUri);
        response.setResults(List.of(result));

        return response;
    }
}
