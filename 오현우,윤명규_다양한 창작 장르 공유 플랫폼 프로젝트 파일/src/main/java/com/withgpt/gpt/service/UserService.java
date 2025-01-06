package com.withgpt.gpt.service;

import com.withgpt.gpt.model.Comment;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.PostCategory;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final CommentService commentService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, CommentService commentService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.passwordEncoder = passwordEncoder;
    }

    // 유저 등록 메서드
    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호 암호화
        userRepository.save(user); // 유저 저장
    }

    // 이메일로 유저 조회 (Optional 반환)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email); // 이메일로 유저를 조회
    }

    // 유저를 반드시 찾아야 하는 경우 (예외 처리 포함)
    public User findByEmail(String email) {
        return getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }


    // 닉네임에 특정 문자열이 포함된 사용자를 찾음
    public List<User> findUsersByNicknameContaining(String nickname) {
        return userRepository.findByNicknameContainingIgnoreCase(nickname);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        userRepository.deleteByEmail(email); // 이메일로 사용자 삭제
    }


}
