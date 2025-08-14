package com.koundary.domain.myPage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class MyScrapItemResponse {
    private Long postId;
    private String title;
    private String contentPreview;
    private LocalDateTime scrappedAt;
}
