package com.withgpt.gpt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpellCheckRequest {
    private String content;  // 맞춤법 검사를 할 문장을 저장하는 필드
}
