package com.koundary.domain.scrap.controller;

import com.koundary.domain.scrap.dto.ScrapResponse;
import com.koundary.domain.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    // 스크랩 추가
    @PostMapping("/posts/{postId}/scrap")
    public ResponseEntity<Void> addScrap(@PathVariable Long postId) {
        scrapService.addScrap(postId);
        return ResponseEntity.ok().build();
    }

    // 스크랩 삭제
    @DeleteMapping("/posts/{postId}/scrap")
    public ResponseEntity<Void> removeScrap(@PathVariable Long postId) {
        scrapService.removeScrap(postId);
        return ResponseEntity.noContent().build();
    }

    // 스크랩 토글
    @PostMapping("/posts/{postId}/scrap/toggle")
    public ResponseEntity<ScrapResponse> toggleScrap(@PathVariable Long postId) {
        return ResponseEntity.ok(scrapService.toggleScrap(postId));
    }
}
