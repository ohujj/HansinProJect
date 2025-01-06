package com.withgpt.gpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.withgpt.gpt.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostId(Long postId);

    List<Comment> findByAuthor(String author);
}
