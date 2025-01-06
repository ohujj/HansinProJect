package com.withgpt.gpt.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Setter
@Getter
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String author;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;

    @Transient  // 데이터베이스에 저장되지 않음
    private String formattedDate;

    public Comment() {
        this.createdDate = LocalDateTime.now(); // 댓글 생성 시 현재 시간 설정
    }

    // 포맷된 날짜를 반환하는 getter 메서드
    public String getFormattedDate() {
        if (this.createdDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return this.createdDate.format(formatter);
        }
//        return formattedDate;
        return "";
    }

    // 포맷된 날짜를 설정하는 setter 메서드
//    public void setFormattedDate(String formattedDate) {
//        this.formattedDate = formattedDate;
//    }
}
