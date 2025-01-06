package com.withgpt.gpt.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class OpenAiService {
    private final String openAiApiKey;
    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    // application.yml에서 키를 주입받음
    public OpenAiService(@Value("${openai.api.key}") String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
    }

    public String generateComment(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiApiKey);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", new JSONArray(Arrays.asList(
                new JSONObject().put("role", "system").put("content", "글을보고 요약과 감상평을 2줄 이내로 남겨줘!."),
                new JSONObject().put("role", "user").put("content", userMessage)
        )));

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(openAiUrl, HttpMethod.POST, entity, String.class);

        JSONObject jsonResponse = new JSONObject(response.getBody());

        // JSON 응답에서 AI의 답변을 추출
        return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }
}
