package com.koundary.domain.auth.controller;

import com.koundary.domain.auth.dto.FindIdRequest;
import com.koundary.domain.auth.dto.ResetConfirmRequest;
import com.koundary.domain.auth.dto.ResetRequest;
import com.koundary.domain.auth.service.AuthPasswordService;
import com.koundary.global.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/reset-password")
@RequiredArgsConstructor
public class AuthPasswordController {

    private final AuthPasswordService authPasswordService;

    @PostMapping("/request")
    public ResponseEntity<MessageResponse> request(@RequestBody ResetRequest req,
                                                   @RequestHeader(value = "X-Real-IP", required = false) String realIp,
                                                   @RequestHeader(value = "CF-Connecting-IP", required = false) String cfIp,
                                                   @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                                   @RequestHeader(value = "User-Agent", required = false) String ua) {
        String ip = firstNonBlank(cfIp, realIp, xff, "0.0.0.0");
        authPasswordService.issuePasswordResetToken(req.getUniversityEmail(), ip, ua);
        // 계정 존재 여부와 관계없이 동일한 메시지(열거 방지)
        return ResponseEntity.ok(new MessageResponse("재설정 링크를 메일로 보냈습니다."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<MessageResponse> confirm(@RequestBody ResetConfirmRequest req) {
        authPasswordService.resetPasswordWithToken(req.getToken(), req.getNewPassword(), req.getConfirmPassword());
        return ResponseEntity.ok(new MessageResponse("비밀번호가 변경되었습니다."));
    }

    private String firstNonBlank(String... arr) {
        for (String s : arr) if (s != null && !s.isBlank()) return s;
        return null;
    }

}
