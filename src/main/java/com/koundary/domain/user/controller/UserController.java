package com.koundary.domain.user.controller;

import com.koundary.domain.user.dto.signup.*;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.domain.user.service.UserService;
import com.koundary.domain.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final VerificationService verificationService;

    /**
     * 아이디 중복확인
     *
     * @param CheckLoginIdDto 아이디 중복확인 체크를 위한 dto
     * @return 아이디 중복확인 결과 엔티티 (bool, message)
     */
    @PostMapping("/check-loginId")
    public ResponseEntity<CheckAvailablityResponse> checkLoginId(@RequestBody CheckLoginIdRequest CheckLoginIdDto) {
        //System.out.println(CheckLoginIdDto.getLoginId());
        return ResponseEntity.ok(userService.checkLoginIdDuplicate(CheckLoginIdDto));
    }

    /**
     * 닉네임 중복확인
     *
     * @param CheckNicknameDto 닉네임 중복확인 체크를 위한 dto
     * @return 닉네임 중복확인 결과 엔티티 (bool, message)
     */
    @PostMapping("/check-nickname")
    public ResponseEntity<CheckAvailablityResponse> checkNickname(@RequestBody CheckNicknameRequest CheckNicknameDto) {
        //System.out.println(CheckNicknameDto.getNickname());
        return ResponseEntity.ok(userService.checkNicknameDuplicate(CheckNicknameDto));
    }

    /*  Verification Controller 부분에 구현함
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
     */

    /**
     * 회원가입 요청
     *
     * @param SignupDto 회원가입 요청 dto
     * @return 회원가입 완료 메시지
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupMessageResponse> signup(@RequestBody SignupRequest SignupDto) {
        userService.signup(SignupDto);
        return ResponseEntity.ok(new SignupMessageResponse("회원가입이 완료되었습니다."));
    }
}
