package com.koundary.domain.user.controller;

import com.koundary.domain.user.dto.signup.*;
import com.koundary.domain.user.service.UserService;
import com.koundary.domain.verification.dto.EmailRequest;
import com.koundary.domain.verification.dto.EmailVerifyRequest;
import com.koundary.domain.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final VerificationService verificationService;

    @PostMapping("/check-loginId")
    public ResponseEntity<CheckAvailablityResponse> checkLoginId(@RequestBody CheckLoginIDRequest dto) {
        return ResponseEntity.ok(userService.checkLoginIdDuplicate(dto));
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<CheckAvailablityResponse> checkNickname(@RequestBody CheckNicknameRequest dto) {
        return ResponseEntity.ok(userService.checkNicknameDuplicate(dto));
    }

    @PostMapping("/email/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequest dto) {
        verificationService.sendVerificationCode(dto.getEmail());
        return ResponseEntity.ok("인증 코드가 전송되었습니다. 이메일을 확인해주세요.");
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequest dto) {
        verificationService.verifyCode(dto.getEmail(), dto.getCode());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupMessageResponse> signup(@RequestBody SignupRequest dto) {
        userService.signup(dto);
        return ResponseEntity.ok(new SignupMessageResponse("회원가입이 완료되었습니다."));
    }
}
