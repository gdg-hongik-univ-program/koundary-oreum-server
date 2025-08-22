package com.koundary.domain.auth.controller;

import com.koundary.domain.auth.dto.FindIdResponse;
import com.koundary.domain.user.service.UserService;
import com.koundary.domain.verification.dto.EmailRequest;
import com.koundary.domain.verification.dto.EmailVerifyRequest;
import com.koundary.domain.verification.service.VerificationService;
import com.koundary.global.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/find-loginId")
public class AuthIdController {

    private final VerificationService verificationService;
    private final UserService userService;

    @PostMapping("/send-code")
    public ResponseEntity<MessageResponse> sendVerificationCode(@RequestBody EmailRequest request) {
        verificationService.sendVerificationCode(request.getEmail());
        return ResponseEntity.ok(new MessageResponse("인증번호가 발송되었습니다."));
    }

    public ResponseEntity<FindIdResponse> verifyCode(@RequestBody EmailVerifyRequest request) {
        verificationService.verifyCode(request.getEmail(), request.getCode());

        String username = userService.findUsernameByEmail(request.getEmail());
        return ResponseEntity.ok(new FindIdResponse(username));
    }
}
