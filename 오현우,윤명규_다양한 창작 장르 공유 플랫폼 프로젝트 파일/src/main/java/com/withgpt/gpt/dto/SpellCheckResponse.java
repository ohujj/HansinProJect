package com.withgpt.gpt.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SpellCheckResponse {
    private List<Suggestion> suggestions;
    private String message;  // 메시지 필드 추가
    private String html; // 수정된 HTML 추가

    // 기본 생성자
    public SpellCheckResponse() {
        this.suggestions = new ArrayList<>();
    }

    // 메시지와 제안을 받는 생성자 추가
    public SpellCheckResponse(String message, List<Suggestion> suggestions) {
        this.message = message;
        this.suggestions = suggestions;
    }

    // Suggestion 클래스
    @Getter
    @Setter
    public static class Suggestion {
        private String original;
        private String corrected;

        @Override
        public String toString() {
            return "원본: " + original + ", 수정: " + corrected;
        }
    }
}
