package com.withgpt.gpt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorEmail;  // 작가 이메일
    private String subscriberEmail;  // 구독자 이메일

    @Enumerated(EnumType.STRING)
    private PostCategory category;  // 구독한 게시판 카테고리 (소설, 시, 에세이 등)

    // 기본 생성자 (JPA 요구사항)
    public Subscription() {}

    // subscriberEmail, authorEmail, category를 받는 생성자
    public Subscription(String subscriberEmail, String authorEmail, PostCategory category) {
        this.subscriberEmail = subscriberEmail;
        this.authorEmail = authorEmail;
        this.category = category;
    }

    // subscriberEmail, authorEmail을 받는 생성자
    public Subscription(String subscriberEmail, String authorEmail) {
        this.subscriberEmail = subscriberEmail;
        this.authorEmail = authorEmail;
    }
}
