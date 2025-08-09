package com.koundary.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentUpdateRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;
}
