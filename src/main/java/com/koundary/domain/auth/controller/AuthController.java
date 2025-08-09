package com.koundary.domain.auth.controller;

import com.koundary.domain.auth.service.AuthService;
import com.koundary.domain.user.dto.LoginRequest;
import com.koundary.domain.user.dto.LoginResponse;
import com.koundary.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 controller
 * 로그인 및 로그아웃 기능 제공
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(token);
        authService.logout(userId);
        return ResponseEntity.ok("Logout successful");
    }
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginResponse response = authService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }


}
