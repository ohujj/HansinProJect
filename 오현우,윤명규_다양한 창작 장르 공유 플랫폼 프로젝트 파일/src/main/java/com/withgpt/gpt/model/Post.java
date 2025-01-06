package com.withgpt.gpt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 10000) // 내용의 최대 길이 수정 1000 -> 10000
    private String content;

    private String author; // 이메일을 저장하는 author 필드

    private int viewCount;

    private int likeCount;

    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @Column(length = 512)
    private String imageUrl;
    private String thumbnail; // <-- 추가해봄

    private String fontClass; // 추가

    @Transient
    private String formattedDate;

    public Post() {
        this.createdDate = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeTracking> likes = new ArrayList<>();

    // 포맷된 날짜를 반환하는 메서드
    public String getFormattedDate() {
        if (this.createdDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return this.createdDate.format(formatter);
        }
        return "";
    }
}
