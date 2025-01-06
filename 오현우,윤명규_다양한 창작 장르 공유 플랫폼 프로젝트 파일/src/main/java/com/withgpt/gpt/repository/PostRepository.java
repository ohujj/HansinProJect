package com.withgpt.gpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.PostCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByCategory(PostCategory category, Pageable pageable);

    Page<Post> findAllByOrderByLikeCountDesc(Pageable pageable);

    @Query("SELECT p.author FROM Post p WHERE p.id = :postId")
    String findAuthorByPostId(@Param("postId") Long postId);

    // 추가된 메서드: postId로 카테고리 찾기
    @Query("SELECT p.category FROM Post p WHERE p.id = :postId")
    PostCategory findCategoryByPostId(@Param("postId") Long postId);

    @Query("SELECT p.author FROM Post p WHERE p.id = :postId AND p.category = :category")
    String findAuthorByPostIdAndCategory(@Param("postId") Long postId, @Param("category") PostCategory category);
    // 날짜 범위 필터링
    // 카테고리별로 날짜 순서로 정렬된 게시글 가져오기
    Page<Post> findByCategoryOrderByCreatedDateDesc(PostCategory category, Pageable pageable);

    // 카테고리별로 좋아요 순서로 정렬된 게시글 가져오기
    Page<Post> findByCategoryOrderByLikeCountDesc(PostCategory category, Pageable pageable);

    // 카테고리별로 조회수 순서로 정렬된 게시글 가져오기
    Page<Post> findByCategoryOrderByViewCountDesc(PostCategory category, Pageable pageable);

    // 카테고리와 날짜 범위로 게시글을 필터링하고 날짜 순으로 정렬
    Page<Post> findByCategoryAndCreatedDateBetweenOrderByCreatedDateDesc(PostCategory category, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);


    // 카테고리와 날짜 범위로 게시글을 필터링하고 좋아요 순으로 정렬
    Page<Post> findByCategoryAndCreatedDateBetweenOrderByLikeCountDesc(PostCategory category, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);


    // 카테고리와 날짜 범위로 게시글을 필터링하고 조회수 순으로 정렬
    Page<Post> findByCategoryAndCreatedDateBetweenOrderByViewCountDesc(PostCategory category, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);

    List<Post> findByAuthor(String authorEmail);

    Optional<Post> findByIdAndCategory(Long id, PostCategory category);




}