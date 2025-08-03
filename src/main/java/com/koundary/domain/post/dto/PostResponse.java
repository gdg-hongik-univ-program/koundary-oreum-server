package com.koundary.domain.post.dto;

import java.time.LocalDateTime;

public record PostResponse(
        Long postId,
        String boardCode,
        String title,
        String content,
        Long userId,
        LocalDateTime createdAt
) {}
