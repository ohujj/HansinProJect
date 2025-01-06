package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.User;
import com.withgpt.gpt.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        // 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인된 사용자가 있는지 확인
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails;
        String welcomeMessage;

        if (isLoggedIn) {
            // 로그인된 사용자의 이메일로 User 객체 조회
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> optionalUser = userRepository.findByEmail(userDetails.getUsername()); // 이메일로 사용자 조회

            if (optionalUser.isPresent()) {
                User loggedInUser = optionalUser.get();

                // User의 nickname을 사용하여 환영 메시지 생성
                String nickname = loggedInUser.getNickname() != null ? loggedInUser.getNickname() : loggedInUser.getName();
                welcomeMessage = nickname + "님 환영합니다!";
            } else {
                welcomeMessage = "Guest님 환영합니다!";
            }
        } else {
            // 로그인되지 않은 경우 메시지 설정
            welcomeMessage = "Guest님 환영합니다!";
        }

        // 모델에 메시지와 로그인 여부 추가
        model.addAttribute("welcomeMessage", welcomeMessage);
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "index";
    }
}
