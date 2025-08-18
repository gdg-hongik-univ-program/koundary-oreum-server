package com.koundary.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long postId,
        String boardCode,
        String title,
        String content,
        Long userId,
        String nickname,          // 작성자 닉네임
        String profileImageUrl,   // 작성자 프로필 이미지 (선택)
        List<String> imageUrls,   // 게시글 이미지 URL 리스트
        LocalDateTime createdAt
) {
    public static PostResponse from(com.koundary.domain.post.entity.Post post) {
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
                post.getCreatedAt()
        );
    }
}