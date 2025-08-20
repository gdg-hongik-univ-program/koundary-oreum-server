package com.koundary.domain.post.controller;

import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.service.PostService;
import com.koundary.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
        log.info("📨 게시글 작성 요청: boardCode={}, title={}, userId={}",
                boardCode, request.title(), userDetails.getUserId());

        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(postService.createPost(boardCode, request, userId));
    }

    // ✅ 게시글 목록 페이징 조회 (12개/페이지, 최신순)
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @PathVariable String boardCode,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("📄 게시글 목록 조회: boardCode={}, page={}, size={}",
                boardCode, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(postService.getPostsByBoard(boardCode, pageable));
    }
}