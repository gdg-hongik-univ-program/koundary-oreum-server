package com.koundary.domain.post.dto;

import java.util.List;

public record PostCreateRequest(
        String title, //
        String content,
        Boolean isInformation, //정보글 유무
        List<String> imageUrls //이미지 URL
) {}
