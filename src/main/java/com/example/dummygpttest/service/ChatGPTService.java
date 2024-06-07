package com.example.dummygpttest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class ChatGPTService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/completions";
    private static final String API_KEY = "sk-proj-6D0GO1wuvYAs4P3SEJTXT3BlbkFJqqd5jpCHxl1bLMamkQJ9";  // Replace with your actual OpenAI API key
    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);

    public String getReport(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        JSONObject request = new JSONObject();
        request.put("prompt", prompt);
        request.put("max_tokens", 100);

        HttpEntity<String> entity = new HttpEntity<>(request.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(OPENAI_API_URL, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject responseBody = new JSONObject(response.getBody());
            return responseBody.getJSONArray("choices").getJSONObject(0).getString("text");
        } else {
            logger.error("Failed to get response from OpenAI: " + response.getStatusCode() + " - " + response.getBody());
            throw new RuntimeException("Failed to get response from OpenAI");
        }
    }
}
