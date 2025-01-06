package com.withgpt.gpt.repository;

import com.withgpt.gpt.model.LikeTracking;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeTracking, Long> {

    // 특정 유저와 게시글에 대한 좋아요 기록을 삭제하는 메서드
    void deleteByUserAndPost(User user, Post post);

    // 유저와 게시글을 기반으로 좋아요를 찾는 메서드
    Optional<LikeTracking> findByUserAndPost(User user, Post post);

    // 특정 유저가 좋아요한 게시글을 가져오는 메서드
    List<LikeTracking> findByUser(User user);

    // 특정 게시글의 좋아요 수를 세는 메서드
    int countByPost(Post post);  // 추가된 메서드
}
