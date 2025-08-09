package com.koundary.domain.comment.controller;

import com.koundary.domain.comment.dto.CommentResponse;
import com.koundary.domain.comment.dto.CreateCommentRequest;
import com.koundary.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Void> createComment(@RequestBody CreateCommentRequest request,
                                              @RequestHeader("X-USER-ID") Long userId) {
        commentService.createComment(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getUserComments(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(commentService.getCommentsByUser(userId));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getPostComments(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}
