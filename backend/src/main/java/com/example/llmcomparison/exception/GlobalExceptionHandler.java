package com.example.llmcomparison.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception e) {
        System.err.println("❌ Uncaught exception: " + e.getMessage());
        return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
    }
}
