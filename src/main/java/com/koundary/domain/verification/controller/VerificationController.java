package com.koundary.domain.verification.controller;

import com.koundary.domain.verification.dto.EmailRequest;
import com.koundary.domain.verification.dto.EmailVerifyRequest;
import com.koundary.domain.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody EmailRequest dto) {
        verificationService.sendVerificationCode(dto.getEmail());
        return ResponseEntity.ok("인증코드가 전송되었습니다. 메일을 확인해주세요.");
    }

    @PostMapping("verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequest dto) {
        verificationService.verifyCode(dto.getEmail(), dto.getCode());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }
}
