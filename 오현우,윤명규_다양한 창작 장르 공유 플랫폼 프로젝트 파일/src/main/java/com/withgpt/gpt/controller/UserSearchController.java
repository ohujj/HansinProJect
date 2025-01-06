package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.User;
import com.withgpt.gpt.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class UserSearchController {

    private final UserService userService;

    public UserSearchController(UserService userService) {
        this.userService = userService;
    }

    // 검색 화면 표시
    @GetMapping("/userSearch")
    public String showSearchPage() {
        return "userSearch"; // userSearch.html 템플릿을 반환
    }

    // 닉네임을 기반으로 사용자 검색
    @GetMapping("/search")
    public String searchUsers(@RequestParam("nickname") String nickname, Model model) {
        // 닉네임을 포함하는 사용자 목록 가져오기
        List<User> users = userService.findUsersByNicknameContaining(nickname);
        model.addAttribute("users", users);
        return "userSearchResults"; // 검색 결과를 보여줄 userSearchResults.html 템플릿 반환
    }
}