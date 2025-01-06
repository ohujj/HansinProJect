package com.withgpt.gpt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/passportKey")
public class PassportKeyController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getPassportKey() {
        String url = "https://search.naver.com/search.naver?query=맞춤법검사기";

        // RestTemplate을 사용해 PassportKey 추출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // PassportKey 추출 로직
        String passportKey = extractPassportKey(response.getBody());

        if (passportKey != null) {
            Map<String, String> result = new HashMap<>();
            result.put("passportKey", passportKey);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String extractPassportKey(String htmlContent) {
        // PassportKey 추출을 위한 정규 표현식 또는 파싱 로직 구현
        // 정규 표현식을 활용해 passportKey 추출
        Pattern pattern = Pattern.compile("passportKey=(\\w+)&");
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            return matcher.group(1); // 첫 번째 매칭된 그룹 반환
        }
        return null;
    }
}
