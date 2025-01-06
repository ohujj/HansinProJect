package com.withgpt.gpt.controller;

import com.withgpt.gpt.dto.SpellCheckRequest;
import com.withgpt.gpt.dto.SpellCheckResponse;
import com.withgpt.gpt.service.SpellCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/spellcheck")
public class SpellCheckController {

    private static final Logger logger = LoggerFactory.getLogger(SpellCheckController.class);  // Logger 선언

    private final SpellCheckService spellCheckService;

    public SpellCheckController(SpellCheckService spellCheckService) {
        this.spellCheckService = spellCheckService;
    }

    @PostMapping
    public ResponseEntity<SpellCheckResponse> checkSpelling(@RequestBody SpellCheckRequest request) {
        logger.info("맞춤법 검사 요청: {}", request.getContent());

        try {
            SpellCheckResponse response = spellCheckService.checkSpelling(request);

            // response가 비어있으면 메시지를 설정
            String message = response.getSuggestions().isEmpty() ? "수정 사항이 없습니다." : "수정 사항이 있습니다.";
            response.setMessage(message);

            logger.info("맞춤법 검사 결과: {}", response.getSuggestions());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 요청: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new SpellCheckResponse("잘못된 요청입니다.", new ArrayList<>()));
        } catch (Exception e) {
            logger.error("맞춤법 검사 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SpellCheckResponse("서버 오류가 발생했습니다.", new ArrayList<>()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("예외 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다.");
    }
}
