package com.example.llmcomparison.dto;

import java.util.List;

public class ImageScoreRequest {
    private String prompt;
    private List<String> images;

    public ImageScoreRequest() {}

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
