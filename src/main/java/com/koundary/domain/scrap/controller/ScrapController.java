package com.koundary.domain.scrap.controller;

import com.koundary.domain.scrap.dto.ScrapMessageResponse;
import com.koundary.domain.scrap.exception.DuplicateScrapException;
import com.koundary.domain.scrap.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/scrap")
public class ScrapController {

    private final ScrapService scrapService;

    /**
     * ✅ 스크랩 토글
     * - 현재 스크랩 상태에 따라 추가/삭제를 자동 수행
     * - 응답 메시지: "스크랩 완료" | "스크랩 해제 완료"
     */
    @PostMapping("/toggle")
    public ResponseEntity<ScrapMessageResponse> toggle(@PathVariable Long postId) {
        boolean nowScrapped = scrapService.toggleScrap(postId);
        String msg = nowScrapped ? "스크랩 완료" : "스크랩 해제 완료";
        return ResponseEntity.ok(new ScrapMessageResponse(msg));
    }

    // ---------------------------
    // (선택) 하위 호환용 엔드포인트
    // 기존 프론트가 POST/DELETE를 호출해도 동작하도록 토글로 위임
    // ---------------------------

    /** @deprecated 토글 방식으로 전환: POST /posts/{id}/scrap/toggle 사용 권장 */
    @Deprecated
    @PostMapping
    public ResponseEntity<ScrapMessageResponse> createScrap(@PathVariable Long postId) {
        boolean nowScrapped = scrapService.toggleScrap(postId);
        String msg = nowScrapped ? "스크랩 완료" : "스크랩 해제 완료";
        return ResponseEntity.ok(new ScrapMessageResponse(msg));
    }

    /** @deprecated 토글 방식으로 전환: POST /posts/{id}/scrap/toggle 사용 권장 */
    @Deprecated
    @DeleteMapping
    public ResponseEntity<ScrapMessageResponse> deleteScrap(@PathVariable Long postId) {
        boolean nowScrapped = scrapService.toggleScrap(postId);
        String msg = nowScrapped ? "스크랩 완료" : "스크랩 해제 완료";
        return ResponseEntity.ok(new ScrapMessageResponse(msg));
    }

    // ====== 예외 핸들러 (선택) ======
    // toggleScrap에서는 보통 DuplicateScrapException이 발생하지 않지만,
    // 프로젝트 전반의 일관성을 위해 남겨둬도 무방.
    @ExceptionHandler(DuplicateScrapException.class)
    public ResponseEntity<ScrapMessageResponse> handleDuplicateScrapException(DuplicateScrapException ex) {
        return ResponseEntity.status(409).body(new ScrapMessageResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ScrapMessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ScrapMessageResponse(ex.getMessage()));
    }
}
