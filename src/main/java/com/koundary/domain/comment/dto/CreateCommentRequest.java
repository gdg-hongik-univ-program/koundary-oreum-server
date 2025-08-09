package com.koundary.domain.comment.dto;

import lombok.Getter;

@Getter
public class CreateCommentRequest {
    private Long postId;
    private String content;
}
