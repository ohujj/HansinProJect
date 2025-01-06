package com.withgpt.gpt.controller;

import com.withgpt.gpt.dto.PostDto;
import com.withgpt.gpt.repository.UserRepository;
import com.withgpt.gpt.service.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.withgpt.gpt.model.Comment;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.PostCategory;
import com.withgpt.gpt.model.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/novel")
public class NovelController {

    private static final Logger logger = LoggerFactory.getLogger(NovelController.class);

    private final PostService postService;
    private final CommentService commentService;
    private final UserService userService;
    private final ImageGenerationService imageGenerationService;
    private final SpellCheckService spellCheckService;
    private final SubscriptionService subscriptionService;
    private final FileService fileService;
    private final OpenAiService openAiService; // OpenAiService 필드 추가

    public NovelController(PostService postService, CommentService commentService, UserService userService,
                           ImageGenerationService imageGenerationService, SpellCheckService spellCheckService,
                           SubscriptionService subscriptionService, FileService fileService, OpenAiService openAiService) {
        this.postService = postService;
        this.commentService = commentService;
        this.userService = userService;
        this.imageGenerationService = imageGenerationService;
        this.spellCheckService = spellCheckService;
        this.subscriptionService = subscriptionService;
        this.fileService = fileService;
        this.openAiService = openAiService;
    }

    // 소설 게시글 목록 페이지
    @GetMapping
    public String listPosts(Model model,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "sort", defaultValue = "date") String sort,
                            @RequestParam(name = "month", required = false) Integer month) {

        Pageable pageable = PageRequest.of(page, 10);
        int currentYear = LocalDate.now().getYear();

        // 월별 필터링 조건 설정: month가 null일 경우 현재 월로 설정
        if (month == null) {
            month = LocalDate.now().getMonthValue(); // 현재 월로 설정
        }

        LocalDate startDate = LocalDate.of(currentYear, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 게시글 가져오기: 월 필터링이 있는 경우와 없는 경우 처리
        Page<Post> posts = postService.getPostsByCategoryAndMonth(PostCategory.NOVEL, pageable, startDate, endDate, sort);

        // 게시글의 작성자 닉네임 설정
        Page<PostDto> postDtos = posts.map(post -> {
            PostDto dto = new PostDto();
            dto.setId(post.getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            dto.setViewCount(post.getViewCount());
            dto.setLikeCount(post.getLikeCount());
            dto.setImageUrl(post.getImageUrl());

            Optional<User> user = userService.getUserByEmail(post.getAuthor());
            dto.setNickname(user.map(User::getNickname).orElse("Unknown"));
            return dto;
        });

        model.addAttribute("posts", postDtos);
        model.addAttribute("sort", sort);
        model.addAttribute("month", month);

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

        return "posts/novel";
    }


    // 새로운 글 작성 페이지
    @GetMapping("/new")
    public String showCreatePostForm(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);
        } else {
            model.addAttribute("username", "Guest");
        }

        model.addAttribute("post", new Post());
        return "posts/createPost";
    }

    // 글 작성 처리
    @PostMapping("/create")
    public String createPost(@RequestParam("title") String title,
                             @RequestParam("content") String content,
                             @RequestParam("fontClass") String fontClass,
                             @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
                             @RequestParam(value = "imageUrl", required = false) String imageUrl,
                             Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        String author = principal.getName();

        try {
            // 1. 파일 업로드 처리
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                // 사용자가 업로드한 파일을 저장하고, imageUrl을 업데이트
                imageUrl = fileService.saveFile(thumbnailFile);
                logger.info("사용자가 업로드한 파일 저장: {}", imageUrl);
            }
            // 2. AI 이미지가 존재할 경우, 저장된 파일명으로 imageUrl을 업데이트
            else if (imageUrl != null && !imageUrl.isEmpty()) {
                logger.info("AI 이미지 URL 사용 전: {}", imageUrl);
                // 여기에서 AI 이미지 URL로 파일이 서버에 저장된 후의 경로로 업데이트
                imageUrl = fileService.saveAIImageFromUrl(imageUrl);
                logger.info("AI 이미지 저장 후 경로: {}", imageUrl);
            } else {
//                throw new RuntimeException("이미지가 선택되지 않았습니다.");
                imageUrl = "src/main/resources/static/uploads/images/photo-1518277980269-c1eb88ad9693.avif";
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return "posts/createPost";  // 오류 발생 시 다시 작성 페이지로 리턴
        }

        // 3. postService를 호출하여 게시글 생성
        Post newPost = postService.createPost(title, content, author, PostCategory.NOVEL, fontClass, imageUrl);

        // AI 댓글 생성
        String aiCommentContent;
        try {
            aiCommentContent = openAiService.generateComment(content); // AI 댓글 생성 메서드 호출
        } catch (Exception e) {
            model.addAttribute("errorMessage", "AI 댓글 생성 중 오류가 발생했습니다.");
            return "posts/createPost"; // 오류 발생 시 다시 작성 페이지로 리턴
        }

        // 댓글 엔티티 생성 및 저장
        Comment aiComment = new Comment();
        aiComment.setPost(newPost); // 새로 생성된 게시글 연결
        aiComment.setContent(aiCommentContent);
        aiComment.setAuthor("AI 친구");

        // 댓글 저장
        commentService.saveComment(newPost.getId(), aiComment); // AI 댓글 저장

        return "redirect:/novel";  // 성공적으로 생성 후 게시글 목록 페이지로 이동
    }

    // 게시글 상세 페이지
    @GetMapping("/{id}")
    public String viewPost(@PathVariable(name = "id") Long id, Model model, HttpSession session) {
        Post post = postService.getPostById(id);

        // 조회수 처리
        Set<Long> viewedPosts = (Set<Long>) session.getAttribute("viewedPosts");
        if (viewedPosts == null) {
            viewedPosts = new HashSet<>();
        }
        if (!viewedPosts.contains(id)) {
            postService.incrementViewCount(id);
            viewedPosts.add(id);
            session.setAttribute("viewedPosts", viewedPosts);
        }

        // 좋아요 상태 확인
        boolean liked = postService.hasUserLikedPost(id, session);
        model.addAttribute("liked", liked);

        // 로그인 여부 확인
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isLoggedIn = principal instanceof UserDetails;
        model.addAttribute("isLoggedIn", isLoggedIn);

        // 작성자 정보 추가
        Optional<User> authorUser = userService.getUserByEmail(post.getAuthor());
        String authorNickname = authorUser.map(User::getNickname).orElse("Unknown");
        Long authorId = authorUser.map(User::getId).orElse(null); // 작성자 ID 가져오기
        model.addAttribute("authorNickname", authorNickname);
        model.addAttribute("authorId", authorId);

        // 현재 로그인한 사용자가 글 작성자인지 확인
        boolean isAuthor = false;
        if (isLoggedIn) {
            UserDetails userDetails = (UserDetails) principal;
            isAuthor = post.getAuthor().equals(userDetails.getUsername());
        }
        model.addAttribute("isAuthor", isAuthor);
        model.addAttribute("post", post);

        // 댓글 리스트 추가
        var comments = commentService.getCommentsByPostId(id); // 댓글 서비스에서 댓글 가져오기
        model.addAttribute("comments", comments);

        return "posts/novelDetail";
    }







    // 댓글 작성 처리
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable(name = "id") Long postId,
                             @RequestParam("content") String content,
                             Principal principal, Model model) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        String authorEmail = principal.getName();
        User user = userService.findByEmail(authorEmail);
        String authorNickname = user.getNickname();

        Post post = postService.getPostById(postId);
        Comment comment = new Comment();
//        comment.setPost(post);
        comment.setContent(content);
        comment.setAuthor(authorNickname);

        commentService.saveComment(postId, comment);
        return "redirect:/novel/" + postId;
    }

    // 월별 필터링
    @GetMapping("/posts/filter")
    public String filterPostsByMonth(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "sort", defaultValue = "date") String sort,
            Model model,
            @RequestParam(defaultValue = "0") int page) {

        // month가 null 또는 문자열 "null"일 경우 현재 월로 설정
        if (month == null || month.equals("null")) {
            month = String.format("%02d", LocalDate.now().getMonthValue()); // 현재 월 설정
        }

        Pageable pageable = PageRequest.of(page, 10);

        LocalDate startDate = null;
        LocalDate endDate = null;
        int year = LocalDate.now().getYear(); // 현재 연도

        // 입력된 월이 존재할 경우 시작일과 종료일 설정
        if (month != null) {
            startDate = LocalDate.of(year, Integer.parseInt(month), 1); // 월과 연도에 맞게 시작일 설정
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // 해당 월의 종료일 설정
        }

        // postService를 사용해 월별 필터링 및 정렬된 게시글을 가져옴
        Page<Post> posts;
        if (startDate != null && endDate != null) {
            posts = postService.getPostsByCategoryAndMonth(PostCategory.NOVEL, pageable, startDate, endDate, sort);
        } else {
            posts = postService.getPostsByCategorySorted(PostCategory.NOVEL, pageable, sort);
        }

        // 모델에 게시글 추가
        model.addAttribute("posts", posts);
        model.addAttribute("month", month);
        model.addAttribute("sort", sort);

        return "posts/list";
    }


//    @PostMapping("/{id}/like")
//    public String likePost(@PathVariable(name = "id") Long id, HttpSession session) {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (!(principal instanceof UserDetails)) {
//            return "redirect:/login";
//        }
//
//        postService.toggleLike(id, session);
//        return "redirect:/novel/" + id;
//    }

    // 파일 요청 처리
    @GetMapping("/files/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") String fileName) {

        try {
            Resource resource = fileService.loadFileAsResource(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            logger.error("파일을 찾을 수 없습니다: " + fileName, ex);
            return ResponseEntity.badRequest().build();
        }
    }
}
