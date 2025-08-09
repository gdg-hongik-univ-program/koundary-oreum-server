package com.koundary.domain.comment.service;

import com.koundary.domain.comment.dto.CommentResponse;
import com.koundary.domain.comment.dto.CreateCommentRequest;
import com.koundary.domain.comment.entity.Comment;
import com.koundary.domain.comment.repository.CommentRepository;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long userId, CreateCommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글 입니다."));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(request.getContent())
                .build();

        commentRepository.save(comment);
    }

    public List<CommentResponse> getCommentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        return commentRepository.findAllByUser(user)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        return commentRepository.findAllByPost(post)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }
}
