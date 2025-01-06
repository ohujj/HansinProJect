package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.PostCategory;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.service.PostService;
import com.withgpt.gpt.service.SubscriptionService;
import com.withgpt.gpt.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService; // UserService 주입

    @Autowired
    private PostService postService;

    @Autowired
    private SubscriptionService subscriptionService;

    // 회원가입 양식 열기
    @GetMapping("/users/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // 새로운 User 객체 추가
        return "register"; // register.html 반환
    }

    // 회원가입 처리
    @PostMapping("/users/register")
    public String registerUser(User user) {
        userService.registerUser(user); // 사용자 등록 처리
        return "redirect:/"; // 회원가입 후 메인 페이지로 리디렉션
    }

    @GetMapping("/user/{userId}")
    public String getUserDetail(@PathVariable("userId") Long userId, Model model, Principal principal) {
        // 사용자 정보 가져오기
        User user = getUserById(userId);

        // 로그인한 사용자의 이메일 가져오기
        String loggedInEmail = getLoggedInUserEmail(principal);

        // 구독 여부 확인
        boolean isSubscribed = isUserSubscribed(loggedInEmail, user.getEmail());

        // 사용자 이메일을 기준으로 모든 게시글 목록을 가져옴
        List<Post> posts = postService.getPostsByAuthor(user.getEmail()); // 이메일을 기준으로 게시글 가져오기

        // 게시글의 작성자 닉네임을 저장할 맵 생성
        Map<Long, String> authorNicknames = new HashMap<>();
        for (Post post : posts) {
            User postAuthor = userService.getUserByEmail(post.getAuthor()) // 수정된 부분
                    .orElseThrow(() -> new NoSuchElementException("해당 이메일로 사용자를 찾을 수 없습니다: " + post.getAuthor()));
            authorNicknames.put(post.getId(), postAuthor.getNickname()); // postId를 키로 사용
        }

        // 사용자가 자주 이용한 게시판 카테고리 계산
        PostCategory preferredCategory = postService.getPreferredCategory(user.getEmail());


        // 모델에 데이터 추가
        model.addAttribute("user", user);
        model.addAttribute("isSubscribed", isSubscribed);
        model.addAttribute("posts", posts); // 게시글 목록 추가
        model.addAttribute("authorNicknames", authorNicknames); // 작성자 닉네임 추가
        model.addAttribute("nickname", user.getNickname()); // 작성자 닉네임 추가(즐겨찾는 게시판 표시용)
        model.addAttribute("preferredCategory", preferredCategory); // 자주 이용한 게시판 카테고리 추가


        return "userDetail"; // 사용자 상세 정보 페이지로 이동
    }

    // 사용자 ID로 사용자 정보 가져오기
    private User getUserById(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 로그인한 사용자의 이메일 가져오기
    private String getLoggedInUserEmail(Principal principal) {
        return (principal != null) ? principal.getName() : null;
    }

    // 사용자가 해당 작성자를 구독했는지 여부 확인
    private boolean isUserSubscribed(String loggedInEmail, String authorEmail) {
        if (loggedInEmail != null) {
            return subscriptionService.isSubscribed(loggedInEmail, authorEmail);
        }
        return false;
    }


    @PostMapping("/user/delete")
    public String deleteUser(Principal principal, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = principal.getName(); // 현재 로그인한 사용자의 이메일 가져오기
        userService.deleteUserByEmail(email);
        SecurityContextHolder.clearContext(); // 로그아웃 처리
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "회원탈퇴가 정상적으로 처리되었습니다.");
        return "redirect:/";
    }

}
