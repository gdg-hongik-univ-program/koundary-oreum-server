package com.koundary.domain.post.controller;

import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.dto.PostUpdateRequest;
import com.koundary.domain.post.service.PostService;
import com.koundary.global.dto.PageResponse;
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

import java.util.List;

import static org.apache.logging.log4j.util.StringBuilders.equalsIgnoreCase;

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
    /*@GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @PathVariable String boardCode,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("📄 게시글 목록 조회: boardCode={}, page={}, size={}",
                boardCode, pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(postService.getPostsByBoard(boardCode, pageable));
    }

     */

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @PathVariable String boardCode,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("📄 게시글 목록 조회: boardCode={}, page={}, size={}",
                boardCode, pageable.getPageNumber(), pageable.getPageSize());

        // NATIONALITY/UNIVERSITY는 로그인 사용자 기준으로 필터링
        if ("NATIONALITY".equalsIgnoreCase(boardCode) || "UNIVERSITY".equalsIgnoreCase(boardCode)) {
            if (userDetails == null) {
                // 401로 처리하고 싶으면 커스텀 예외 + @ControllerAdvice 사용
                throw new IllegalStateException("로그인이 필요합니다.");
            }
            Page<PostResponse> mine = postService.getMyPostsByBoard(boardCode, userDetails.getUserId(), pageable);
            return ResponseEntity.ok(mine);
        }

        // 그 외 보드는 전체 노출
        Page<PostResponse> all = postService.getPostsByBoard(boardCode, pageable);
        return ResponseEntity.ok(all);
    }

    // src/main/java/com/koundary/domain/post/controller/PostController.java
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String boardCode,
            @PathVariable Long postId
    ) {
        PostResponse response = postService.getPost(boardCode, postId);
        return ResponseEntity.ok(response);
    }
    // ✅ 게시글 수정 (원본/복사본 동시 처리)
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("✏️ 게시글 수정: boardCode={}, postId={}, userId={}",
                boardCode, postId, userDetails.getUserId());
        Long userId = userDetails.getUserId();
        PostResponse updated = postService.updatePost(boardCode, postId, request, userId);
        return ResponseEntity.ok(updated);
    }

    // ✅ 게시글 삭제 (세트 동시 삭제)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("🗑️ 게시글 삭제: boardCode={}, postId={}, userId={}",
                boardCode, postId, userDetails.getUserId());
        Long userId = userDetails.getUserId();
        postService.deletePost(boardCode, postId, userId);
        return ResponseEntity.noContent().build();
    }
}