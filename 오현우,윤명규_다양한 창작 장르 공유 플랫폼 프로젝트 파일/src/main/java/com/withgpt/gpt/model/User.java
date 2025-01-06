package com.withgpt.gpt.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "Users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name; // 실명
    private String nickname; // 닉네임으로 사용
    private int age;
    private String phoneNumber;
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    private Gender gender; // Gender는 ENUM으로 정의

    private String password; // 암호화된 비밀번호

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    private int totalSponsorshipAmount = 0; // 받은 총 후원 금액 (기본값 0)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LikeTracking> likes = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sponsorship> sponsorships; // 사용자가 받은 후원 리스트
}
