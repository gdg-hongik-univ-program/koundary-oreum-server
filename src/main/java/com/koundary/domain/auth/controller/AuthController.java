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
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader
    ) {
        String access = jwtTokenProvider.resolveToken(request);

        if (access != null && jwtTokenProvider.validateAccessToken(access)) {
            Long userId = jwtTokenProvider.getUserIdFromAccessToken(access);
            authService.logout(userId);
            return ResponseEntity.ok("Logout successful (by access)");
        }

        if (refreshHeader != null && jwtTokenProvider.validateRefreshToken(refreshHeader)) {
            Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshHeader);
            authService.logout(userId);
            return ResponseEntity.ok("Logout successful (by refresh)");
        }

        return ResponseEntity.badRequest().body("유효한 토큰이 없습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        // ✅ Refresh 전용 검증 사용
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        LoginResponse response = authService.reissue(refreshToken);
        return ResponseEntity.ok(response);
    }
}