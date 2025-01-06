package com.withgpt.gpt.repository;

import com.withgpt.gpt.model.LikeTracking;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeTrackingRepository extends JpaRepository<LikeTracking, Long> {
    Optional<LikeTracking> findByPostAndUser(Post post, User user);
}
