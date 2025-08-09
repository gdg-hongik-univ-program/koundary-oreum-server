package com.koundary.domain.post.dto;

import java.util.List;

public record PostCreateRequest(
        String title,
        String content,
        Boolean isInformation,
        List<String> imageUrls
) {}
