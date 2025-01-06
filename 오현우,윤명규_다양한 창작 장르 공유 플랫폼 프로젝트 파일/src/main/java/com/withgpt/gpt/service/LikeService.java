package com.withgpt.gpt.service;

import com.withgpt.gpt.model.LikeTracking;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Transactional
    public void saveLike(User user, Post post) {
//        LikeTracking like = new LikeTracking();
//        like.setUser(user);
//        like.setPost(post);
//        likeRepository.save(like);

        System.out.println("좋아요 추가 시도: 사용자=" + user.getEmail() + ", 게시글=" + post.getTitle());

        Optional<LikeTracking> existingLike = likeRepository.findByUserAndPost(user, post);

        if (existingLike.isEmpty()) {
            LikeTracking like = new LikeTracking();
            like.setUser(user);
            like.setPost(post);
            likeRepository.save(like);
            System.out.println("좋아요 저장 완료: 사용자=" + user.getEmail() + ", 게시글=" + post.getTitle());
        } else {
            System.out.println("이미 좋아요를 누른 게시글: 사용자=" + user.getEmail() + ", 게시글=" + post.getTitle());
        }
    }
}

