package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User()); // User 클래스의 객체 생성
        return "login"; // login.html 반환
    }

}
