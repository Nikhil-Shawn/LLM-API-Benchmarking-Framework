package com.example.llmcomparison.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
