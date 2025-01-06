package com.withgpt.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SponsorshipDTO {
    private String sponsorNickname;
    private int amount;
    private LocalDateTime sponsorshipDate;
}