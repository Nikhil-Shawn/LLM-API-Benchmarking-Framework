package com.example.llmcomparison.controller;

import org.springframework.web.bind.annotation.*;
import java.io.*;

@RestController
@RequestMapping("/api/eval")
public class CodeEvalController {

    @PostMapping("/python")
    public String runPython(@RequestBody EvalRequest request) {
        try {
            // Save code to temp file
            File file = File.createTempFile("code", ".py");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(request.code());
                writer.write("\n");
                writer.write(request.testCases());
            }

            // Run the code
            Process process = new ProcessBuilder("python3", file.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public record EvalRequest(String code, String testCases) {}
}
