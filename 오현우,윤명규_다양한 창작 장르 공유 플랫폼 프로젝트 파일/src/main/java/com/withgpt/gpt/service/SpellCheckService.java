package com.withgpt.gpt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.withgpt.gpt.dto.SpellCheckRequest;
import com.withgpt.gpt.dto.SpellCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpellCheckService {

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SpellCheckService.class);

    private static final String SPELL_CHECK_URL = "https://m.search.naver.com/p/csearch/ocontent/util/SpellerProxy";
    private static final String SEARCH_URL = "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=맞춤법검사기";

    public SpellCheckService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SpellCheckResponse checkSpelling(SpellCheckRequest request) {
        String content = request.getContent();
        String passportKey = getPassportKey();

        SpellCheckResponse response = sendSpellCheckRequest(content, passportKey);

        // 키가 유효하지 않으면 새 키를 발급받아 다시 요청
        if (response.getMessage() != null && response.getMessage().contains("유효한 키가 아닙니다.")) {
            passportKey = getPassportKey();
            response = sendSpellCheckRequest(content, passportKey);
        }

        // 메시지 처리 로직을 서비스 레벨에서 처리
        String message = response.getSuggestions().isEmpty() ? "수정 사항이 없습니다." : "수정 사항이 있습니다.";
        response.setMessage(message);

        return response;
    }

    private SpellCheckResponse sendSpellCheckRequest(String content, String passportKey) {
        String params = "?q=" + content + "&passportKey=" + passportKey + "&where=nexearch&color_blindness=0";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                SPELL_CHECK_URL + params,
                HttpMethod.GET,
                entity,
                String.class
        );

        logger.info("API 응답: {}", response.getBody());

        return parseSpellCheckResponse(response.getBody());
    }

    private SpellCheckResponse parseSpellCheckResponse(String responseBody) {
        SpellCheckResponse response = new SpellCheckResponse();
        List<SpellCheckResponse.Suggestion> suggestions = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode result = root.path("message").path("result");
            JsonNode errors = result.path("errata");

            // errata_count가 1 이상일 경우 오류 처리
            int errataCount = result.path("errata_count").asInt();
            if (errataCount > 0) {
                // 원본 단어 추출을 위해 origin_html을 사용
                String originHtml = result.path("origin_html").asText();
                Pattern originalPattern = Pattern.compile("<span class='result_underline'>(.*?)</span>");
                Matcher originalMatcher = originalPattern.matcher(originHtml);

                // 수정된 단어는 html에서 추출
                String html = result.path("html").asText();
                Pattern correctedPattern = Pattern.compile("<em class='[a-zA-Z0-9_]+'>(.*?)</em>");
                Matcher correctedMatcher = correctedPattern.matcher(html);

                // 원본 단어와 수정된 단어를 모두 찾아서 매핑
                while (originalMatcher.find() && correctedMatcher.find()) {
                    SpellCheckResponse.Suggestion suggestion = new SpellCheckResponse.Suggestion();
                    suggestion.setOriginal(originalMatcher.group(1)); // 원본 단어 추출
                    suggestion.setCorrected(correctedMatcher.group(1)); // 수정된 단어 추출
                    suggestions.add(suggestion);
                }

                // HTML 결과 저장
                response.setHtml(html);
            }

            // suggestions 설정
            response.setSuggestions(suggestions);
            logger.info("맞춤법 검사 결과: {}", suggestions);

        } catch (Exception e) {
            logger.error("JSON 파싱 중 오류 발생", e);
        }

        return response;
    }


    public String getPassportKey() {
        ResponseEntity<String> response = restTemplate.getForEntity(SEARCH_URL, String.class);

        String passportKey = null;
        String regex = "passportKey=([a-zA-Z0-9]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(response.getBody());

        if (matcher.find()) {
            passportKey = matcher.group(1);
        }

        logger.info("발급된 passportKey: {}", passportKey);
        return passportKey;
    }
}
