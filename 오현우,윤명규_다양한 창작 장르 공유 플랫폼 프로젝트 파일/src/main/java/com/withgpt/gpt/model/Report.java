package com.withgpt.gpt.model;

import com.withgpt.gpt.model.ReportedType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportedType reportedType;  // "POST" 또는 "COMMENT"

    private Long reportedId;  // 신고된 게시글 또는 댓글의 ID
    private String reason;    // 신고 사유 (스팸, 욕설 등)
    private String reporter;  // 신고자 (username)

    private String reportedAuthor;  // 신고된 글/댓글의 작성자 닉네임
    private String reportedCategory;  // 신고된 게시글의 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;  // 중첩 객체로 변경

    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();  // 신고 일시

}
