package com.withgpt.gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class PapagoTranslationService {

    private static final Logger logger = LoggerFactory.getLogger(PapagoTranslationService.class);

    @Value("${naver.papago.client-id}")
    private String clientId;

    @Value("${naver.papago.client-secret}")
    private String clientSecret;

    @Value("${naver.papago.detect-url}")
    private String detectLangUrl;

    @Value("${naver.papago.translate-url}")
    private String translateUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PapagoTranslationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String detectLanguage(String inputText) {
        try {
            HttpEntity<MultiValueMap<String, String>> requestEntity = createDetectLangRequestEntity(inputText);

            ResponseEntity<String> response = restTemplate.exchange(
                    detectLangUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return parseDetectedLanguage(response.getBody());
        } catch (Exception e) {
            logger.error("언어 감지 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    public String translateText(String inputText, String sourceLang, String targetLang) {
        try {
            HttpEntity<MultiValueMap<String, String>> requestEntity = createTranslationRequestEntity(inputText, sourceLang, targetLang);

            ResponseEntity<String> response = restTemplate.exchange(
                    translateUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return parseTranslatedText(response.getBody());
        } catch (Exception e) {
            logger.error("번역 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }

    private HttpEntity<MultiValueMap<String, String>> createDetectLangRequestEntity(String inputText) {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Content-Type 설정

        MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
        bodyParams.add("query", inputText);

        return new HttpEntity<>(bodyParams, headers);
    }

    private HttpEntity<MultiValueMap<String, String>> createTranslationRequestEntity(String inputText, String sourceLang, String targetLang) {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Content-Type 설정

        MultiValueMap<String, String> bodyParams = new LinkedMultiValueMap<>();
        bodyParams.add("source", sourceLang);
        bodyParams.add("target", targetLang);
        bodyParams.add("text", inputText);

        return new HttpEntity<>(bodyParams, headers);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);
        return headers;
    }

    private String parseDetectedLanguage(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("langCode").asText();
        } catch (Exception e) {
            logger.error("언어 감지 결과 파싱 중 오류: {}", e.getMessage());
            return null;
        }
    }

    private String parseTranslatedText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("message").path("result").path("translatedText").asText();
        } catch (Exception e) {
            logger.error("번역 결과 파싱 중 오류: {}", e.getMessage());
            return null;
        }
    }
}
