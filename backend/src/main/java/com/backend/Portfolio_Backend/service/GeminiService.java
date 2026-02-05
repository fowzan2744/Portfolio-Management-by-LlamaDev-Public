package com.backend.Portfolio_Backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for integrating with Python Gemini AI Microservice
 */
@Service
public class GeminiService {

    @Value("${gemini.python.service.url:http://localhost:5000}")
    private String pythonServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        System.out.println("=== GeminiService Initialized ===");
        System.out.println("Python Service URL: " + pythonServiceUrl);
        System.out.println("Checking Python service health...");
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                pythonServiceUrl + "/health", Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null) {
                System.out.println("Python Service Status: " + body.get("status"));
                System.out.println("API Key Configured: " + body.get("api_key_configured"));
            }
        } catch (Exception e) {
            System.out.println("⚠ Python service not reachable: " + e.getMessage());
        }
        System.out.println("================================");
    }

    /**
     * Check if Python Gemini service is configured and available
     */
    public boolean isConfigured() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                pythonServiceUrl + "/health", Map.class);
            Map<String, Object> body = response.getBody();
            
            if (body == null) {
                System.err.println("Python service health check returned null body");
                return false;
            }
            
            Object apiKeyConfigured = body.get("api_key_configured");
            System.out.println("DEBUG: api_key_configured value = " + apiKeyConfigured + " (type: " + (apiKeyConfigured != null ? apiKeyConfigured.getClass().getName() : "null") + ")");
            
            // Handle both Boolean and String representations
            if (apiKeyConfigured instanceof Boolean) {
                return (Boolean) apiKeyConfigured;
            } else if (apiKeyConfigured != null) {
                return Boolean.parseBoolean(apiKeyConfigured.toString());
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Python service health check failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generate AI insights using Python Gemini microservice
     * 
     * @param prompt The analysis prompt
     * @return JSON string response from Gemini
     */
    public String generateInsights(String prompt) {
        if (!isConfigured()) {
            throw new IllegalStateException("Python Gemini service is not configured");
        }

        try {
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Build request body for Python service
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Call Python microservice
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonServiceUrl + "/api/ai/analyze",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Python service returned status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Python AI service call failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate AI insights: " + e.getMessage(), e);
        }
    }
}
