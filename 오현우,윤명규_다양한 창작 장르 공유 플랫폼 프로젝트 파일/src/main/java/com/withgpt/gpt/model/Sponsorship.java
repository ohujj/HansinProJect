package com.withgpt.gpt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sponsorship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author; // 후원을 받은 사용자 (작가)

    @ManyToOne
    @JoinColumn(name = "sponsor_id", nullable = false)
    private User sponsor; // 후원한 사용자

    private int amount;
    private LocalDateTime sponsorshipDate;
}
