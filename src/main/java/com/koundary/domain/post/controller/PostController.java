package com.koundary.domain.post.controller;

import com.koundary.domain.post.dto.PostCreateRequest;
import com.koundary.domain.post.dto.PostResponse;
import com.koundary.domain.post.dto.PostUpdateRequest;
import com.koundary.domain.post.service.PostService;
import com.koundary.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardCode}/posts")
public class PostController {

    private final PostService postService;

    // =========================
    // 생성 - JSON 전용 (기존)
    // =========================
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String boardCode,
            @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("📨 게시글 작성(JSON): boardCode={}, title={}, userId={}",
                boardCode, request.title(), userDetails.getUserId());

        Long userId = userDetails.getUserId();
        return ResponseEntity.ok(postService.createPost(boardCode, request, userId));
    }

    // =========================
    // 생성 - 멀티파트(JSON + files)
    // =========================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPostWithFiles(
            @PathVariable String boardCode,
            @RequestPart("data") PostCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        log.info("📨 게시글 작성(MULTIPART): boardCode={}, title={}, userId={}, files={}",
                boardCode, request.title(), userId, images == null ? 0 : images.size());

        PostResponse res = postService.createPostWithFiles(boardCode, request, userId, images);
        return ResponseEntity.ok(res);
    }

    // ✅ 게시글 목록 페이징 조회 (12개/페이지, 최신순)
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(
            @PathVariable String boardCode,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("📄 게시글 목록 조회: boardCode={}, page={}, size={}",
                boardCode, pageable.getPageNumber(), pageable.getPageSize());

        Long viewerUserId = (userDetails != null) ? userDetails.getUserId() : null;

        // NATIONALITY/UNIVERSITY는 로그인 사용자 기준으로 필터링
        if ("NATIONALITY".equalsIgnoreCase(boardCode) || "UNIVERSITY".equalsIgnoreCase(boardCode)) {
            if (viewerUserId == null) {
                throw new IllegalStateException("로그인이 필요합니다.");
            }
            Page<PostResponse> mine = postService.getMyPostsByBoard(boardCode, viewerUserId, pageable);
            return ResponseEntity.ok(mine);
        }

        // 그 외 보드는 전체 노출 + viewerUserId로 isScrapped 채움
        Page<PostResponse> all = postService.getPostsByBoard(boardCode, pageable, viewerUserId);
        return ResponseEntity.ok(all);
    }

    // ✅ 게시글 상세 (viewerUserId로 isScrapped 채움)
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerUserId = (userDetails != null) ? userDetails.getUserId() : null;
        PostResponse response = postService.getPost(boardCode, postId, viewerUserId);
        return ResponseEntity.ok(response);
    }

    // =========================
    // 수정 - JSON 전용 (기존)
    // =========================
    @PatchMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("게시글 수정(JSON): boardCode={}, postId={}, userId={}",
                boardCode, postId, userDetails.getUserId());
        Long userId = userDetails.getUserId();
        PostResponse updated = postService.updatePost(boardCode, postId, request, userId);
        return ResponseEntity.ok(updated);
    }

    // =========================
    // 수정 - 멀티파트(JSON + files)
    // =========================
    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> updatePostWithFiles(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @RequestPart("data") PostUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        log.info("게시글 수정(MULTIPART): boardCode={}, postId={}, userId={}, files={}",
                boardCode, postId, userId, newImages == null ? 0 : newImages.size());

        PostResponse res = postService.updatePostWithFiles(boardCode, postId, request, userId, newImages);
        return ResponseEntity.ok(res);
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
