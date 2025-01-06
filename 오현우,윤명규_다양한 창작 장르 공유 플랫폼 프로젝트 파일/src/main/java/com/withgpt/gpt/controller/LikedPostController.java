package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.service.LikeService;
import com.withgpt.gpt.service.PostService;
import com.withgpt.gpt.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class LikedPostController {

    private static final Logger logger = LoggerFactory.getLogger(LikedPostController.class);  // 로거 선언

    private final UserService userService;
    private final PostService postService;
    private final LikeService likeService;

    public LikedPostController(UserService userService, PostService postService, LikeService likeService) {
        this.userService = userService;
        this.postService = postService;
        this.likeService = likeService;
    }

    @GetMapping("/likedPosts")
    public String getLikedPosts(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        // 사용자 정보 조회 (Optional<User> 사용)
        Optional<User> optionalUser = userService.getUserByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();  // Optional에서 User 추출

            List<Post> likedPosts = postService.getLikedPostsByUser(user);
            System.out.println("좋아요한 게시글 수: " + likedPosts.size());

            // 디버깅용 로그 추가
            likedPosts.forEach(post -> logger.info("좋아요 한 게시글: " + post.getTitle()));

            model.addAttribute("likedPosts", likedPosts);
            return "likedPosts";  // 좋아요한 게시글을 보여주는 페이지
        } else {
            logger.error("사용자를 찾을 수 없습니다: " + email);
            model.addAttribute("error", "사용자를 찾을 수 없습니다.");
            return "error";  // 에러 페이지로 리다이렉트
        }
    }

    @PostMapping("/novel/{id}/like")
    public String likePost(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return "redirect:/login";
        }

        System.out.println("좋아요 API 호출됨: postId=" + id);

        String email = userDetails.getUsername();
        User user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postService.getPostById(id);

        likeService.saveLike(user, post);
        postService.toggleLike(id, session);

        return "redirect:/novel/{id}";
    }
}
