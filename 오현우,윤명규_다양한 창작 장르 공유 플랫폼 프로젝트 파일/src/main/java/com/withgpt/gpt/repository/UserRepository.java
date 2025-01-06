package com.withgpt.gpt.repository;

import com.withgpt.gpt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    // 닉네임에 특정 문자열이 포함된 사용자를 찾음 (대소문자 구분 없음)
    List<User> findByNicknameContainingIgnoreCase(String nickname);
    void deleteByEmail(String email);
}
