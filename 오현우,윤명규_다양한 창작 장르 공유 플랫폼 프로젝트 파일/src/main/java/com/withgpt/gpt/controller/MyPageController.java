package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.*;
import com.withgpt.gpt.service.CommentService;
import com.withgpt.gpt.service.PostService;
import com.withgpt.gpt.service.SubscriptionService; // 구독 서비스 추가
import com.withgpt.gpt.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class MyPageController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    private final SubscriptionService subscriptionService; // 구독 서비스 추가

    public MyPageController(UserService userService, PostService postService, CommentService commentService, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
        this.subscriptionService = subscriptionService; // 구독 서비스 초기화
    }

    // 로그인된 사용자의 이메일을 가져오는 메서드
    private String getLoggedInUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : null;
    }

    // MyPage 접속
    @GetMapping("/mypage")
    public String showMyPage(Model model) {
        String email = getLoggedInUserEmail();
        if (email != null) {
            User user = userService.getUserByEmail(email).orElse(null);  // User 객체 가져오기
            if (user != null) {
                model.addAttribute("nickname", user.getNickname());  // 닉네임 추가

                // 사용자의 선호 게시판 추가
                PostCategory preferredCategory = postService.getPreferredCategory(email);
                if (preferredCategory != null) {
                    model.addAttribute("preferredCategory", preferredCategory);
                } else {
                    model.addAttribute("preferredCategory", "활동 기록 없음");
                }
            }
        } else {
            return "redirect:/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        }

        // 로그인 여부와 환영 메시지 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails;
        String welcomeMessage;

        if (isLoggedIn) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> optionalUser = userService.getUserByEmail(userDetails.getUsername());
            welcomeMessage = optionalUser.map(user -> user.getNickname() + "님 환영합니다!").orElse("Guest님 환영합니다!");
        } else {
            welcomeMessage = "Guest님 환영합니다!";
        }

        model.addAttribute("welcomeMessage", welcomeMessage);
        model.addAttribute("isLoggedIn", isLoggedIn);
        
        return "mypage"; // mypage.html 템플릿으로 이동
    }

    // 사용자 정보 보기
    @GetMapping("/userInfo")
    public String showUserInfo(Model model) {
        String email = getLoggedInUserEmail();
        if (email != null) {
            User user = userService.getUserByEmail(email).orElse(null);
            model.addAttribute("user", user);
            model.addAttribute("nickname", user.getNickname());  // 닉네임 추가
        } else {
            return "redirect:/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        }
        return "userInfo"; // userInfo.html 템플릿으로 이동
    }

    // 사용자 정보 수정 페이지
    @GetMapping("/userInfoUpdate")
    public String showUserInfoUpdate(Model model) {
        String email = getLoggedInUserEmail();
        if (email != null) {
            User user = userService.getUserByEmail(email).orElse(null);
            model.addAttribute("user", user);
            model.addAttribute("nickname", user.getNickname());  // 닉네임 추가
        } else {
            return "redirect:/login"; // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
        }
        return "userInfoUpdate"; // userInfoUpdate.html 템플릿으로 이동
    }

    @GetMapping("/myPosts")
    public String showUserPosts(Model model) {
        String email = getLoggedInUserEmail();
        if (email != null) {
            User user = userService.getUserByEmail(email).orElse(null);
            if (user != null) {
                List<Post> posts = postService.getPostsByAuthor(user.getEmail());
                List<Comment> comments = commentService.getCommentsByAuthor(user.getEmail());
                model.addAttribute("posts", posts);
                model.addAttribute("comments", comments);
                model.addAttribute("nickname", user.getNickname());  // 닉네임 추가
            }
        }
        return "myPosts"; // myPage.html로 이동
    }

    @GetMapping("/myComments")
    public String showUserComments(Model model) {
        String email = getLoggedInUserEmail();
        if (email != null) {
            User user = userService.getUserByEmail(email).orElse(null);
            if (user != null) {
                List<Comment> comments = commentService.getCommentsByAuthor(user.getNickname());
                model.addAttribute("comments", comments);
                model.addAttribute("nickname", user.getNickname());  // 닉네임 추가
            }
        }
        return "myComments"; // myComments.html로 이동하도록 설정
    }

    // 구독 목록 보기
    @GetMapping("/mySubscriptions")
    public String showSubscriptions(Model model) {
        String email = getLoggedInUserEmail();
        if (email != null) {
            // 이메일로 구독 목록을 조회하도록 수정
            List<Subscription> subscriptions = subscriptionService.getSubscriptionsBySubscriberEmail(email);
            model.addAttribute("subscriptions", subscriptions);
            User user = userService.getUserByEmail(email).orElse(null);
            model.addAttribute("nickname", user.getNickname());  // 닉네임 추가
        }
        return "mySubscriptions"; // 구독 목록 템플릿으로 이동
    }
}
