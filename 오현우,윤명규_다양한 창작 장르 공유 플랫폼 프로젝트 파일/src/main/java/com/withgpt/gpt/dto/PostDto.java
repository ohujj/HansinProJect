package com.withgpt.gpt.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String fontClass; // 추가
    private int viewCount;
    private int likeCount;
    private String nickname; // 작성자의 닉네임

    // 새로 추가할 필드들
    private String imageUrl; // AI 생성 이미지 또는 업로드된 이미지 URL

}