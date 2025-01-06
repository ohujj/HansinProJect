package com.withgpt.gpt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

//@Service
//public class ImageGenerationService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);  // 로그 추가
//
//    private final RestTemplate restTemplate;
//
//    // application.yml 파일에서 API 키를 읽어옴
//    @Value("${stability.api.key}")
//    private String apiKey;
//
//    private final String API_URL = "https://api.stability.ai/v2beta/stable-image/generate/ultra";  // Stability AI 엔드포인트
//
//    public ImageGenerationService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public String generateImage(String prompt) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + apiKey);  // API 키 설정
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);  // Content-Type 설정
//        headers.set("Accept", "image/*");  // 이미지 형식 응답 허용
//
//        // API 요청 본문
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("prompt", prompt);  // prompt 추가
//        body.add("output_format", "webp");  // 선택적: 원하는 이미지 형식 추가
//
//        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
//
//        try {
//            logger.info("이미지 생성 요청 시작 - 프롬프트: {}", prompt);  // 요청 시작 로그
//            ResponseEntity<byte[]> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, byte[].class);
//
//            // 응답 상태 코드 확인
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                logger.info("이미지 생성 성공 - 상태 코드: {}, Content-Type: {}", response.getStatusCode(), response.getHeaders().getContentType());
//
//                // 이미지를 Base64로 인코딩
//                String base64Image = Base64.getEncoder().encodeToString(response.getBody());
//
//                // 로그에 이미지 크기 출력
//                logger.info("이미지 Base64 인코딩 완료 - 이미지 크기: {} bytes", response.getBody().length);
//
//                // Base64 인코딩된 이미지를 반환
//                return "data:image/webp;base64," + base64Image;
//            } else {
//                logger.error("이미지 생성 실패 - 상태 코드: {}, 응답 본문: {}", response.getStatusCode(), response.getBody());
//                return null;
//            }
//        } catch (Exception e) {
//            logger.error("이미지 생성 중 오류 발생: {}", e.getMessage(), e);
//
//            // HTTP 응답 오류 처리 (추가적으로 발생할 수 있는 다양한 오류 처리)
//            if (e instanceof HttpClientErrorException) {
//                HttpClientErrorException httpEx = (HttpClientErrorException) e;
//                logger.error("HTTP 클라이언트 오류 발생 - 상태 코드: {}, 응답 본문: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
//            } else if (e instanceof HttpServerErrorException) {
//                HttpServerErrorException httpEx = (HttpServerErrorException) e;
//                logger.error("HTTP 서버 오류 발생 - 상태 코드: {}, 응답 본문: {}", httpEx.getStatusCode(), httpEx.getResponseBodyAsString());
//            }
//
//            return null;
//        }
//    }
//}

@Service
public class ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationService.class);
    private final RestTemplate restTemplate;
    private final PapagoTranslationService papagoTranslationService; // Papago 번역 서비스 추가

    // OpenAI API 키를 application.yml에서 가져옴
    @Value("${openai.api.key}")
    private String apiKey;

    private final String OPENAI_IMAGE_API_URL = "https://api.openai.com/v1/images/generations";  // OpenAI DALL-E 엔드포인트

    public ImageGenerationService(RestTemplate restTemplate, PapagoTranslationService papagoTranslationService) {
        this.restTemplate = restTemplate;
        this.papagoTranslationService = papagoTranslationService; // Papago 번역 서비스 초기화
    }

    public String generateImage(String prompt) {
        try {
            // 언어 감지 및 번역 처리
            String detectedLang = papagoTranslationService.detectLanguage(prompt);
            if ("ko".equals(detectedLang)) {
                prompt = papagoTranslationService.translateText(prompt, "ko", "en");
                logger.info("한글을 영어로 번역했습니다: " + prompt);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // API 요청 데이터
            Map<String, Object> requestBody = Map.of(
                    "prompt", prompt,
                    "n", 1,  // 생성할 이미지 수
                    "size", "512x512"  // 이미지 사이즈
            );

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(OPENAI_IMAGE_API_URL, HttpMethod.POST, requestEntity, Map.class);

            // 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.getBody().get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    String imageUrl = (String) dataList.get(0).get("url");
                    logger.info("AI 이미지 생성 성공, URL: " + imageUrl);
                    return imageUrl;
                } else {
                    logger.error("이미지 데이터가 비어있습니다.");
                    return null;
                }
            } else {
                logger.error("이미지 생성 실패: " + response.getBody());
                return null;
            }
        } catch (Exception e) {
            logger.error("이미지 생성 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
}
