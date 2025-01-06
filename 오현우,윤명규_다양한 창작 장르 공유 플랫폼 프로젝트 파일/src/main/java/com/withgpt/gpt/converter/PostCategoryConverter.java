package com.withgpt.gpt.converter;

import com.withgpt.gpt.model.PostCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PostCategoryConverter implements Converter<String, PostCategory> {

    @Override
    public PostCategory convert(String source) {
        try {
            return PostCategory.valueOf(source.toUpperCase()); // 대소문자 구분 없이 Enum 변환
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 값 처리 (예: 400 에러를 발생시킬 수 있음)
        }
    }
}
