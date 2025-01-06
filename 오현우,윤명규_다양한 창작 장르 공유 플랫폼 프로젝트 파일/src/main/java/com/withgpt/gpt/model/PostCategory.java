package com.withgpt.gpt.model;

public enum PostCategory {
    NOVEL, ESSAY, POETRY, DIARY;

    @Override
    public String toString() {
        return name().toLowerCase(); // 소문자로 변환
    }
}
