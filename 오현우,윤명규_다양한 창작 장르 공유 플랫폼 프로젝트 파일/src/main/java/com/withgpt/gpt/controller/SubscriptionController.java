package com.withgpt.gpt.controller;
import com.withgpt.gpt.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.withgpt.gpt.model.PostCategory;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.Subscription;
import com.withgpt.gpt.service.PostService;
import com.withgpt.gpt.service.SubscriptionService;
import com.withgpt.gpt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, PostService postService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.postService = postService;
        this.userService = userService;
    }

    // 로그인된 사용자의 이메일을 가져오는 메서드
    private String getLoggedInUserEmail(Principal principal) {
        return principal.getName();
    }

    @PostMapping("/subscribe/{authorEmail}")
    public String subscribe(@PathVariable String authorEmail, Principal principal) {
        String subscriberEmail = getLoggedInUserEmail(principal);

        // 로그 추가
        logger.info("구독 요청: authorEmail={}, subscriberEmail={}", authorEmail, subscriberEmail);

        // 구독 처리
        subscriptionService.subscribe(authorEmail, subscriberEmail);
        logger.info("구독 성공: authorEmail={}, subscriberEmail={}", authorEmail, subscriberEmail);


        // 구독 후 현재 보고 있는 페이지로 리다이렉트
        return "redirect:/novel"; // 구독 후 리다이렉트할 페이지
    }

    @PostMapping("/unsubscribe/{authorEmail}")
    public String unsubscribe(@PathVariable String authorEmail, Principal principal) {
        String subscriberEmail = getLoggedInUserEmail(principal);

        // 로그 추가
        logger.info("구독 취소 요청: authorEmail={}, subscriberEmail={}", authorEmail, subscriberEmail);

        // 구독 취소 처리
        subscriptionService.unsubscribe(authorEmail, subscriberEmail);

        // 구독 취소 후 현재 보고 있는 페이지로 리다이렉트
        return "redirect:/novel"; // 구독 취소 후 리다이렉트할 페이지
    }

    @GetMapping("/novel/{postId}")
    public String showPostDetail(@PathVariable Long postId, Principal principal, Model model) {
        Post post = postService.getPostById(postId);  // 게시글 정보 조회
        model.addAttribute("post", post);

        String loggedInUserEmail = principal != null ? principal.getName() : null;

        boolean isSubscribed = false;
        if (loggedInUserEmail != null) {
            isSubscribed = subscriptionService.isSubscribed(post.getAuthor(), loggedInUserEmail);
        }

        model.addAttribute("isSubscribed", isSubscribed);
        model.addAttribute("isAuthor", post.getAuthor().equals(loggedInUserEmail));

        return "posts/novelDetail";  // 게시글 상세 페이지로 이동
    }

    @PostMapping("/subscribe-to-author/{authorEmail}")
    public String subscribeToAuthor(
            @PathVariable("authorEmail") String authorEmail,
            Principal principal,
            @RequestParam("postId") Long postId
    ) {
        // 로그인된 사용자 정보 가져오기
        String subscriberEmail = getLoggedInUserEmail(principal);

        // 로그 추가
        logger.info("구독 요청 처리 중: authorEmail={}, subscriberEmail={}, postId={}", authorEmail, subscriberEmail, postId);

        // 구독 서비스 로직 호출
        subscriptionService.subscribe(authorEmail, subscriberEmail);

        // 구독 후 현재 보고 있는 페이지로 리다이렉트
        return "redirect:/novel/" + postId;
    }

    @PostMapping("/unsubscribe-to-author/{authorEmail}")
    public String unsubscribeToAuthor(
            @PathVariable("authorEmail") String authorEmail,
            Principal principal,
            @RequestParam("postId") Long postId
    ) {
        String subscriberEmail = getLoggedInUserEmail(principal);

        logger.info("구독 취소 요청 처리 중: authorEmail={}, subscriberEmail={}, postId={}", authorEmail, subscriberEmail, postId);

        subscriptionService.unsubscribe(authorEmail, subscriberEmail);

        return "redirect:/novel/" + postId;
    }

    @PostMapping("/toggle-subscription/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSubscription(
            @PathVariable("userId") Long userId,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            logger.warn("로그인된 사용자가 없습니다.");
            response.put("errorMessage", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String subscriberEmail = principal.getName();
        logger.info("로그인한 사용자: {}", subscriberEmail);

        // userId를 사용하여 작가 정보를 조회
        Optional<User> authorOptional = userService.findById(userId);
        if (authorOptional.isEmpty()) {
            logger.error("존재하지 않는 사용자입니다: userId={}", userId);
            response.put("errorMessage", "존재하지 않는 사용자입니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        User author = authorOptional.get();
        String authorEmail = author.getEmail(); // 피구독 대상의 이메일

        logger.info("구독 상태 토글 시작: authorEmail={}", authorEmail);

        // 구독 상태를 토글
        boolean isSubscribed = subscriptionService.toggleSubscription(authorEmail, subscriberEmail);

        logger.info("구독 상태가 성공적으로 변경되었습니다.");
        response.put("isSubscribed", isSubscribed);
        response.put("successMessage", "구독 상태가 성공적으로 변경되었습니다!");

        return ResponseEntity.ok(response);
    }




    @GetMapping("/subscription-list")
    public String showSubscriptionList(Model model, Principal principal) {
        if (principal == null) {
            logger.warn("로그인된 사용자가 없습니다.");
            return "redirect:/login";
        }

        String userEmail = principal.getName();
        logger.info("사용자의 구독 목록 조회: {}", userEmail);

        List<Subscription> subscriptions = subscriptionService.getSubscriptionsBySubscriberEmail(userEmail);
        if (subscriptions.isEmpty()) {
            logger.info("구독 목록이 비어있습니다.");
        } else {
            logger.info("구독 목록 조회 성공, {} 개의 구독 있음", subscriptions.size());
        }

        model.addAttribute("subscriptions", subscriptions);
        return "subscriptionList";
    }






}
