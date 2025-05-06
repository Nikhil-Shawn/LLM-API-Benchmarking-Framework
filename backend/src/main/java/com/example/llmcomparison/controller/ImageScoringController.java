package com.example.llmcomparison.controller;

import com.example.llmcomparison.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/image")
public class ImageScoringController {

  private final GeminiService gemini;

  public ImageScoringController(GeminiService gemini) {
    this.gemini = gemini;
  }

  // DTO for incoming request
  public static class ImageScoreRequest {
    private String prompt;        // currently unused
    private List<String> images;  // list of data-URIs or URLs

    public String getPrompt() { return prompt; }
    public void setPrompt(String p) { this.prompt = p; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> i) { this.images = i; }
  }

  // DTO for outgoing response
  public static class ImageScoreResponse {
    private Map<String, Double> scores;
    private String winner;

    public ImageScoreResponse(Map<String,Double> scores, String winner) {
      this.scores = scores;
      this.winner = winner;
    }

    public Map<String,Double> getScores() { return scores; }
    public String getWinner() { return winner; }
  }

  @PostMapping("/score")
  public ResponseEntity<ImageScoreResponse> scoreImages(
      @RequestBody ImageScoreRequest req) throws Exception {

    List<String> images = req.getImages();
    if (images == null || images.size() != 3) {
      throw new IllegalArgumentException("Exactly 3 images (A, B, C) are required.");
    }

    Map<String,Double> scores = new LinkedHashMap<>();
    String[] slots = {"A","B","C"};

    for (int i = 0; i < 3; i++) {
      String raw = gemini.scoreImage(images.get(i));
      double val = Double.parseDouble(raw.trim());
      scores.put(slots[i], val);
    }

    String winner = scores.entrySet().stream()
      .max(Map.Entry.comparingByValue())
      .map(Map.Entry::getKey)
      .orElse("A");

    return ResponseEntity.ok(new ImageScoreResponse(scores, winner));
  }
}
