package com.koundary.domain.scrap.controller;

import com.koundary.domain.scrap.dto.ScrapMessageResponse;
import com.koundary.domain.scrap.exception.DuplicateScrapException;
import com.koundary.domain.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ScrapController {

    private final ScrapService scrapService;

    // 스크랩 추가
    @PostMapping("/posts/{postId}/scrap")
    public ResponseEntity<ScrapMessageResponse> createScrap(@PathVariable Long postId) {
        scrapService.addScrap(postId);
        return ResponseEntity.ok(new ScrapMessageResponse("스크랩 완료"));
    }

    // 스크랩 삭제
    @DeleteMapping("/posts/{postId}/scrap")
    public ResponseEntity<ScrapMessageResponse> deleteScrap(@PathVariable Long postId) {
        scrapService.removeScrap(postId);
        return ResponseEntity.ok(new ScrapMessageResponse("스크랩 해제 완료"));
    }

    @ExceptionHandler(DuplicateScrapException.class)
    public ResponseEntity<ScrapMessageResponse> handleDuplicateScrapException(DuplicateScrapException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ScrapMessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ScrapMessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ScrapMessageResponse(ex.getMessage()));
    }
}
