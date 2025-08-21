package com.koundary.domain.post.dto;

import java.util.List;

public record PostUpdateRequest(
        String title,
        String content,
        Boolean isInformation,
        List<String> imageUrls
) {}
