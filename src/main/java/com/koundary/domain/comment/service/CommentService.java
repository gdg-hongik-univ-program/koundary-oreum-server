package com.koundary.domain.comment.service;

<<<<<<< HEAD
import com.koundary.domain.comment.dto.CommentResponse;
import com.koundary.domain.comment.dto.CreateCommentRequest;
=======
import com.koundary.domain.comment.dto.CommentCreateRequest;
import com.koundary.domain.comment.dto.CommentResponse;
import com.koundary.domain.comment.dto.CommentUpdateRequest;
>>>>>>> b1
import com.koundary.domain.comment.entity.Comment;
import com.koundary.domain.comment.repository.CommentRepository;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
<<<<<<< HEAD
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
=======
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
>>>>>>> b1
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

<<<<<<< HEAD
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
=======
    /** 최상위 댓글 작성 */
    @Transactional
    public CommentResponse addComment(Long postId, Long userId, CommentCreateRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .author(user)
                .content(req.getContent())
                .deleted(false)
                .build();

        Comment saved = commentRepository.save(comment);
        return toDto(saved, userId, 0);
    }

    /** 대댓글 작성 (대댓글의 대댓글은 제한) */
    @Transactional
    public CommentResponse addReply(Long parentCommentId, Long userId, CommentCreateRequest req) {
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!parent.isTopLevel()) {
            throw new IllegalStateException("대댓글의 대댓글은 허용하지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment reply = Comment.builder()
                .post(parent.getPost())
                .author(user)
                .parent(parent)
                .content(req.getContent())
                .deleted(false)
                .build();

        Comment saved = commentRepository.save(reply);
        return toDto(saved, userId, 0);
    }

    /** 게시글의 최상위 댓글 페이지 조회 */
    public Page<CommentResponse> getComments(Long postId, Long currentUserId, Pageable pageable) {
        Page<Comment> page = commentRepository
                .findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(postId, pageable);

        return page.map(c -> toDto(c, currentUserId, commentRepository.countByParent_Id(c.getId())));
    }

    /** 특정 댓글의 대댓글 목록 조회 */
    public List<CommentResponse> getReplies(Long parentCommentId, Long currentUserId) {
        List<Comment> replies = commentRepository.findByParent_IdOrderByCreatedAtAsc(parentCommentId);
        return replies.stream()
                .map(r -> toDto(r, currentUserId, 0))
                .toList();
    }

    /** 댓글 내용 수정 (본인만) */
    @Transactional
    public CommentResponse update(Long commentId, Long userId, CommentUpdateRequest req) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!c.getAuthor().getUserId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }
        if (c.isDeleted()) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }
        c.updateContent(req.getContent());
        return toDto(c, userId, c.isTopLevel() ? commentRepository.countByParent_Id(c.getId()) : 0);
    }

    /** 소프트 삭제 (본인만) */
    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        if (!c.getAuthor().getUserId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        if (!c.isDeleted()) {
            c.softDelete();
        }
    }

    // ------------------ mapper ------------------
    private CommentResponse toDto(Comment c, Long currentUserId, int replyCount) {
        Long authorId = c.getAuthor().getUserId();
        Long postId = c.getPost().getPostId();

        return CommentResponse.builder()
                .commentId(c.getId())
                .postId(postId)
                .authorId(authorId)
                .authorNickname(c.getAuthor().getNickname())
                .authorProfileImage(c.getAuthor().getProfileImage())
                .content(c.isDeleted() ? "(삭제된 댓글입니다)" : c.getContent())
                .deleted(c.isDeleted())
                .mine(currentUserId != null && authorId.equals(currentUserId))
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .replyCount(replyCount)
                .build();
>>>>>>> b1
    }
}
