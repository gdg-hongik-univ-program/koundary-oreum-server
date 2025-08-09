package com.koundary.domain.post.controller;

import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.service.PostService;
import com.koundary.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardCode}/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String boardCode,
            @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("📨 게시글 작성 요청: boardCode={}, title={}, userId={}", boardCode, request.title(), userDetails.getUserId());

        Long userId = userDetails.getUserId();  // JWT 인증된 사용자 ID
        PostResponse response = postService.createPost(boardCode, request, userId);
        return ResponseEntity.ok(response);
    }
}
