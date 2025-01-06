package com.withgpt.gpt.service;

import com.withgpt.gpt.model.*;
import com.withgpt.gpt.repository.LikeRepository;
import com.withgpt.gpt.repository.PostRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final OpenAiService openAiService;

    private final UserService userService;
    private final LikeRepository likeRepository;

    private final CommentService commentService; // CommentService 주입
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    public PostService(PostRepository postRepository, OpenAiService openAiService, UserService userService, LikeRepository likeRepository, CommentService commentService) {
        this.postRepository = postRepository;
        this.openAiService = openAiService;
        this.userService = userService;
        this.likeRepository = likeRepository;
        this.commentService = commentService;
    }

    // 카테고리에 따라 게시글을 가져오는 메서드
    public Page<Post> getPostsByCategory(PostCategory category, Pageable pageable) {
        Page<Post> posts = postRepository.findByCategory(category, pageable);
        posts.forEach(post -> post.setFormattedDate(post.getCreatedDate().format(formatter)));
        return posts;
    }

    // 조회수 증가 메서드
    public void incrementViewCount(Long postId) {
        Post post = getPostById(postId);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    // 좋아요 토글 메서드
    public void toggleLike(Long postId, HttpSession session) {
        Post post = getPostById(postId);

        Set<Long> likedPosts = (Set<Long>) session.getAttribute("likedPosts");
        if (likedPosts == null) {
            likedPosts = new HashSet<>();
        }

        if (likedPosts.contains(postId)) {
            post.setLikeCount(post.getLikeCount() - 1);
            likedPosts.remove(postId);
        } else {
            post.setLikeCount(post.getLikeCount() + 1);
            likedPosts.add(postId);
        }

        session.setAttribute("likedPosts", likedPosts);
        postRepository.save(post);
    }


    // 사용자가 좋아요한 게시글 가져오기
    public List<Post> getLikedPostsByUser(User user) {
        List<LikeTracking> likes = likeRepository.findByUser(user);

        if (likes.isEmpty()) {
            System.out.println("사용자가 좋아요한 게시글이 없습니다: " + user.getEmail());
        }

        return likes.stream()
                .map(LikeTracking::getPost)
                .peek(post -> logger.info("좋아요 한 게시글: " + post.getTitle()))
                .collect(Collectors.toList());
    }

    // 좋아요 여부 확인 메서드

    public boolean hasUserLikedPost(Long postId, HttpSession session) {
        Set<Long> likedPosts = (Set<Long>) session.getAttribute("likedPosts");
        return likedPosts != null && likedPosts.contains(postId);
    }

    // 새로운 게시글 생성 메서드
    public Post createPost(String title, String content, String author, PostCategory category, String fontClass, String imageUrl) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(author);
        post.setCategory(category);
        post.setFontClass(fontClass);
        post.setImageUrl(imageUrl);
        post.setCreatedDate(LocalDateTime.now());
        post.setFormattedDate(post.getCreatedDate().format(formatter));

        return postRepository.save(post);
    }

    // 게시글 수정 메서드
    public void updatePost(Long id, String title, String content) {
        Post post = getPostById(id);
        post.setTitle(title);
        post.setContent(content);
        postRepository.save(post);
    }

    // 게시글 삭제 메서드
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    // 사용자가 작성한 게시글 가져오기
    public List<Post> getPostsByAuthor(String author) {
        List<Post> posts = postRepository.findByAuthor(author);
        posts.forEach(post -> post.setFormattedDate(post.getCreatedDate().format(formatter)));
        return posts;
    }

    // 정렬 기준에 따라 카테고리별로 게시글을 가져오는 메서드 (날짜순, 좋아요순, 조회수순)
    public Page<Post> getPostsByCategorySorted(PostCategory category, Pageable pageable, String sort) {
        switch (sort) {
            case "like":
                return postRepository.findByCategoryOrderByLikeCountDesc(category, pageable);
            case "view":
                return postRepository.findByCategoryOrderByViewCountDesc(category, pageable);
            default:
                return postRepository.findByCategoryOrderByCreatedDateDesc(category, pageable);
        }
    }

    // 월별 필터링 및 정렬 (날짜순, 좋아요순, 조회수순)
    public Page<Post> getPostsByCategoryAndMonth(PostCategory category, Pageable pageable, LocalDate startDate, LocalDate endDate, String sort) {
        // LocalDate를 LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        switch (sort) {
            case "like":
                return postRepository.findByCategoryAndCreatedDateBetweenOrderByLikeCountDesc(category, startDateTime, endDateTime, pageable);
            case "view":
                return postRepository.findByCategoryAndCreatedDateBetweenOrderByViewCountDesc(category, startDateTime, endDateTime, pageable);
            default:
                return postRepository.findByCategoryAndCreatedDateBetweenOrderByCreatedDateDesc(category, startDateTime, endDateTime, pageable);
        }
    }


    // 특정 ID로 게시글을 가져오는 메서드
    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 ID로 게시글을 찾을 수 없습니다: " + id));
    }

    public List<Post> findAllPostsByAuthor(String authorEmail) {
        return postRepository.findByAuthor(authorEmail);
    }

    // 특정 ID로 게시글을 가져오는 메서드
    public List<Post> getPostsByAuthor(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID로 사용자를 찾을 수 없습니다: " + userId));
        return postRepository.findByAuthor(user.getEmail()); // 사용자의 이메일을 기준으로 게시글 검색
    }

    // 사용자가 자주 활동한 게시판 카테고리를 찾는 메서드
    public PostCategory getPreferredCategory(String userEmail) {
        Map<PostCategory, Integer> activityCount = new HashMap<>();

        // 작성한 글들의 카테고리 별로 수를 세기
        List<Post> posts = getPostsByAuthor(userEmail);
        for (Post post : posts) {
            PostCategory category = post.getCategory();
            activityCount.put(category, activityCount.getOrDefault(category, 0) + 1);
        }

        // 작성한 댓글들을 기반으로 해당 게시판의 글 카테고리 별로 수를 세기
        List<Comment> comments = commentService.getCommentsByAuthor(userEmail);
        for (Comment comment : comments) {
            PostCategory category = comment.getPost().getCategory();
            activityCount.put(category, activityCount.getOrDefault(category, 0) + 1);
        }

        // 가장 많이 활동한 게시판 카테고리 찾기
        return activityCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null); // 활동이 없을 경우 null 반환
    }

}

