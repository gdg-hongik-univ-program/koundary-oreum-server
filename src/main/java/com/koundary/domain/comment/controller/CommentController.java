package com.koundary.domain.comment.controller;

import com.koundary.domain.comment.dto.CommentCreateRequest;
import com.koundary.domain.comment.dto.CommentResponse;
import com.koundary.domain.comment.dto.CommentUpdateRequest;
import com.koundary.domain.comment.service.CommentService;
import com.koundary.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    // 최상위 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public CommentResponse createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        return commentService.addComment(postId, user.getUserId(), request);
    }

    // 대댓글 작성
    @PostMapping("/comments/{commentId}/replies")
    public CommentResponse createReply(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentCreateRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        return commentService.addReply(commentId, user.getUserId(), request);
    }

    // 게시글의 최상위 댓글 페이지 조회
    @GetMapping("/posts/{postId}/comments")
    public Page<CommentResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        Long me = (user == null) ? null : user.getUserId();
        return commentService.getComments(postId, me, PageRequest.of(page, size));
    }

    // 특정 댓글의 대댓글 목록
    @GetMapping("/comments/{commentId}/replies")
    public List<CommentResponse> getReplies(
            @PathVariable Long commentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        Long me = (user == null) ? null : user.getUserId();
        return commentService.getReplies(commentId, me);
    }

    // 댓글 수정
    @PatchMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        return commentService.update(commentId, user.getUserId(), request);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @PathVariable Long commentId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        commentService.delete(commentId, user.getUserId());
    }
}
