package com.koundary.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.koundary.domain.post.entity.Post;

public record PostResponse(
        Long postId,
        String boardCode,
        String title,
        String content,
        Long userId,
        String nickname,          // 작성자 닉네임
        String profileImageUrl,   // 작성자 프로필 이미지 (선택)
        List<String> imageUrls,   // 게시글 이미지 URL 리스트
        int scrapCount,           // ✅ 스크랩 수
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getPostId(),
                post.getBoard().getBoardCode(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getUserId(),
                post.getUser().getNickname(),
                post.getUser().getProfileImageUrl(),
                post.getImages().stream()
                        .map(img -> img.getImageUrl())
                        .toList(),
                post.getScrapCount(),     // ✅ 매핑
                post.getCreatedAt()
        );
    }
}
