package com.koundary.domain.scrap.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScrapRequest {

    //스르랩 대상 포스트 아이디
    private Long postId;
}
