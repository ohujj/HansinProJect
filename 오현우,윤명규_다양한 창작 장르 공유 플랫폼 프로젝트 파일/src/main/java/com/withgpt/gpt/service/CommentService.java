package com.withgpt.gpt.service;

import com.withgpt.gpt.model.Comment;
import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.repository.CommentRepository;
import com.withgpt.gpt.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    // 댓글 저장 메소드
    public void saveComment(Long postId, Comment comment) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post with id: " + postId + " not found"));

        // Post와의 연관성 설정
        comment.setPost(post);

        // 댓글 저장
        commentRepository.save(comment);
    }

    // 특정 게시물에 대한 댓글 리스트 가져오기
    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);

        // 댓글 리스트의 날짜 형식을 지정
        comments.forEach(comment -> comment.setFormattedDate(comment.getCreatedDate().format(formatter)));

        return comments;
    }

    // 사용자가 작성한 댓글을 가져오는 메서드
    public List<Comment> getCommentsByAuthor(String author) {
        List<Comment> comments = commentRepository.findByAuthor(author);

        // 댓글 중 Post가 null이 아닌 것만 필터링하고, 날짜 형식 지정
//        return comments.stream()
//                .filter(comment -> comment.getPost() != null)  // post가 null인 경우 필터링
//                .peek(comment -> comment.setFormattedDate(comment.getCreatedDate().format(formatter)))
//                .collect(Collectors.toList());

        comments.forEach(post -> post.setFormattedDate(post.getCreatedDate().format(formatter)));
        return comments;
    }
}
