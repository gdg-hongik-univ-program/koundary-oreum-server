package com.koundary.domain.report.controller;

import com.koundary.domain.report.dto.ReportMessageResponse;
import com.koundary.domain.report.dto.ReportRequest;
import com.koundary.domain.report.repository.ReportRepository;
import com.koundary.domain.report.service.ReportService;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @PostMapping
    public ResponseEntity<ReportMessageResponse> reportPost(
            Authentication auth,
            @Valid @RequestBody ReportRequest request
            ) {
        // 로그인 사용자 식별
        String loginId = auth.getName();
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."));

        // 신고
        reportService.report(user.getUserId(), request);

        // 응답
        return ResponseEntity.ok(new ReportMessageResponse("신고가 접수되었습니다."));
    }

    // 게시글 누적 신고 개수 조회
    @GetMapping("/{postId}/count")
    public ResponseEntity<Integer> countReports(@PathVariable Long postId) {
        return ResponseEntity.ok(reportService.countReports(postId));
    }

    // 내가 신고 했는지 여부
    @GetMapping("/{postId}/me")
    public ResponseEntity<Boolean> didIReport(
            Authentication auth,
            @PathVariable Long postId
    ) {
        String loginId = auth.getName();
        Long userId = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저를 찾을 수 없습니다."))
                .getUserId();
        boolean exists = reportRepository.existsByReporterIdAndPostId(userId, postId);
        return ResponseEntity.ok(exists);
    }
}
