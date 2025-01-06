package com.withgpt.gpt.repository;

import com.withgpt.gpt.model.Sponsorship;
import com.withgpt.gpt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {

    // 후원한 사람(user)으로 후원 내역 조회
    List<Sponsorship> findBySponsor(User sponsor);

    // 후원받은 사람(author)으로 후원 내역 조회
    List<Sponsorship> findByAuthor(User author);

    // 특정 작가에게 후원한 사람의 닉네임으로 후원 내역 검색
    List<Sponsorship> findByAuthorAndSponsorNicknameContaining(User author, String nickname);
}
