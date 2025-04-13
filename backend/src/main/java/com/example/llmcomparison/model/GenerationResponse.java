package com.example.llmcomparison.model;

import java.util.List;

public class GenerationResponse {
    public static class ModelResult {
        private String model;
        private String output;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }

    private List<ModelResult> results;

    public List<ModelResult> getResults() {
        return results;
    }

    public void setResults(List<ModelResult> results) {
        this.results = results;
    }
}