package com.withgpt.gpt.service;

import com.withgpt.gpt.model.Sponsorship;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.repository.SponsorshipRepository;
import com.withgpt.gpt.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SponsorshipService {

    private final SponsorshipRepository sponsorshipRepository;
    private final UserRepository userRepository;

    public SponsorshipService(SponsorshipRepository sponsorshipRepository, UserRepository userRepository) {
        this.sponsorshipRepository = sponsorshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createSponsorship(String userEmail, String authorEmail, int amount) {
        User sponsor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("후원자를 찾을 수 없습니다: " + userEmail));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다: " + authorEmail));

        // 후원 엔티티 생성 및 저장
        Sponsorship sponsorship = new Sponsorship();
        sponsorship.setSponsor(sponsor);
        sponsorship.setAuthor(author);
        sponsorship.setAmount(amount);
        sponsorship.setSponsorshipDate(LocalDateTime.now());

        sponsorshipRepository.save(sponsorship);

        // 작가의 총 후원 금액 업데이트
        author.setTotalSponsorshipAmount(author.getTotalSponsorshipAmount() + amount);
        userRepository.save(author);
    }

    public List<Sponsorship> getSponsorshipsByAuthorEmail(String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다: " + authorEmail));
        return author.getSponsorships();
    }

    public List<Sponsorship> searchSponsorshipsBySponsorNickname(String authorEmail, String nickname) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다: " + authorEmail));
        return sponsorshipRepository.findByAuthorAndSponsorNicknameContaining(author, nickname);
    }
}