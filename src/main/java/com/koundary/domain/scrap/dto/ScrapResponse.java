package com.koundary.domain.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ScrapResponse {
    private boolean scrapped; // true: 스크랩됨, false: 스크랩 해제됨
}
