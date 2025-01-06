package com.withgpt.gpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

@Controller
public class WritedCommentController {

    @GetMapping("/writedComment")
    public String showWritedComment(Model model) {
        // 로그인된 사용자 정보 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isLoggedIn = principal instanceof UserDetails;

        // 사용자 이름 가져오기
        String username = isLoggedIn ? ((UserDetails) principal).getUsername() : "Guest";
        model.addAttribute("username", username);

        // 작성한 글 리스트 가져오기 (임시로 빈 리스트 사용)
        model.addAttribute("comments", new ArrayList<>()); // 실제 데이터는 서비스에서 가져와야 함

        return "writedComment"; // writedComment.html 템플릿으로 이동
    }
}
