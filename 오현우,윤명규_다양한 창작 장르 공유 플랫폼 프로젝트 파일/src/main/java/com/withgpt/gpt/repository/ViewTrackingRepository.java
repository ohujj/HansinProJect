package com.withgpt.gpt.repository;

import com.withgpt.gpt.model.ViewTracking;
import com.withgpt.gpt.model.User;
import com.withgpt.gpt.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewTrackingRepository extends JpaRepository<ViewTracking, Long> {
    Optional<ViewTracking> findByPostAndUser(Post post, User user);
}
