package com.withgpt.gpt.controller;

import com.withgpt.gpt.service.ImageGenerationService;
import com.withgpt.gpt.service.PapagoTranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
public class ImageGenerationApiController {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationApiController.class);  // 로거 추가

    private final ImageGenerationService imageGenerationService;
    private final PapagoTranslationService papagoTranslationService;

    @Autowired
    public ImageGenerationApiController(ImageGenerationService imageGenerationService, PapagoTranslationService papagoTranslationService) {
        this.imageGenerationService = imageGenerationService;
        this.papagoTranslationService = papagoTranslationService;
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        logger.info("이미지 생성 요청: " + prompt);

        // 프롬프트 길이 제한 설정 (255자 제한)
        if (prompt == null || prompt.isBlank()) {
            logger.error("프롬프트가 비어있거나 null입니다.");
            return ResponseEntity.status(400).body(Map.of("error", "프롬프트가 비어있거나 null입니다. 유효한 프롬프트를 입력해주세요."));
        }

        if (prompt.length() > 255) {
            logger.error("프롬프트가 너무 깁니다. 길이: " + prompt.length());
            return ResponseEntity.status(400).body(Map.of("error", "프롬프트가 너무 깁니다. 255자 이하로 입력해주세요."));
        }

        try {
            // 언어 감지 및 번역 처리
            String detectedLang = papagoTranslationService.detectLanguage(prompt);
            if ("ko".equals(detectedLang)) {
                prompt = papagoTranslationService.translateText(prompt, "ko", "en");
                logger.info("한글을 영어로 번역했습니다: " + prompt);
            }

            // 쉼표로 구분된 키워드 분리 및 처리
            String[] keywords = prompt.split(",");
            StringBuilder refinedPrompt = new StringBuilder();

            for (String keyword : keywords) {
                keyword = keyword.trim();
                if (!keyword.isEmpty()) {
                    refinedPrompt.append(keyword).append(" ");
                }
            }

            String finalPrompt = refinedPrompt.toString().trim();
            logger.info("최종 프롬프트: " + finalPrompt);

            // 이미지 생성 요청
            String imageUrl = imageGenerationService.generateImage(finalPrompt);

            if (imageUrl != null && !imageUrl.isBlank()) {
                logger.info("이미지 생성 성공, URL: " + imageUrl);
                return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
            } else {
                logger.error("이미지 생성 실패: 응답이 null이거나 빈 값입니다.");
                return ResponseEntity.status(500).body(Map.of("error", "이미지 생성에 실패했습니다.", "reason", "응답이 null이거나 빈 값입니다."));
            }
        } catch (IllegalArgumentException e) {
            logger.error("잘못된 입력으로 인한 이미지 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(400).body(Map.of("error", "잘못된 입력입니다.", "message", e.getMessage()));
        } catch (HttpClientErrorException e) {
            // 크레딧 부족(402) 처리
            if (e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
                logger.error("크레딧 부족으로 인한 이미지 생성 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
                        "error", "크레딧 부족",
                        "message", "API 크레딧이 부족합니다. 크레딧을 충전한 후 다시 시도해주세요."
                ));
            }
            logger.error("HTTP 클라이언트 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", "HTTP 클라이언트 오류", "message", e.getMessage()));
        } catch (HttpServerErrorException e) {
            logger.error("HTTP 서버 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", "HTTP 서버 오류", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("이미지 생성 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "이미지 생성 중 오류가 발생했습니다.", "message", e.getMessage()));
        }
    }
}

