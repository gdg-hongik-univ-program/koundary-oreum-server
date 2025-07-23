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
    public ResponseEntity<CheckAvailablityResponse> checkLoginId(@RequestBody CheckLoginIdRequest CheckLoginIdDto) {
        return ResponseEntity.ok(userService.checkLoginIdDuplicate(CheckLoginIdDto));
    }

    @PostMapping("/check-nickname")
    public ResponseEntity<CheckAvailablityResponse> checkNickname(@RequestBody CheckNicknameRequest CheckNicknameDto) {
        return ResponseEntity.ok(userService.checkNicknameDuplicate(CheckNicknameDto));
    }

    @PostMapping("/email/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody EmailRequest EmailDto) {
        verificationService.sendVerificationCode(EmailDto.getEmail());
        return ResponseEntity.ok("인증 코드가 전송되었습니다. 이메일을 확인해주세요.");
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody EmailVerifyRequest EmailVerifyDto) {
        verificationService.verifyCode(EmailVerifyDto.getEmail(), EmailVerifyDto.getCode());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupMessageResponse> signup(@RequestBody SignupRequest SignupDto) {
        userService.signup(SignupDto);
        return ResponseEntity.ok(new SignupMessageResponse("회원가입이 완료되었습니다."));
    }
}
