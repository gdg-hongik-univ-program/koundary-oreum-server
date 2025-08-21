package com.koundary.domain.auth.service;

import com.koundary.domain.auth.entity.RefreshToken;
import com.koundary.domain.auth.repository.RefreshTokenRepository;
import com.koundary.domain.user.dto.LoginRequest;
import com.koundary.domain.user.dto.LoginResponse;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.global.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;                 // 사용자 정보 조회 Repository
    private final RefreshTokenRepository refreshTokenRepository; // RefreshToken 저장소
    private final JwtTokenProvider jwtTokenProvider;             // JWT 생성/검증 유틸
    private final PasswordEncoder passwordEncoder;               // 비밀번호 암호화/검증

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 로그인
     * - 아이디/비밀번호 검증
     * - Access/Refresh Token 발급
     * - 기존 Refresh Token 제거 후 신규 토큰 저장
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1) 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(user.getUserId(), "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 4) 기존 RT 제거 후 신규 저장
        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.save(new RefreshToken(
                user.getUserId(),
                refreshToken,
                // ✅ 방법 A: 프로퍼티 단위를 '밀리초(ms)'로 유지하므로 Duration.ofMillis(...)로 더한다
                LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()))
        ));

        // 5) 응답
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUserId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }

    /**
     * 로그아웃
     * - 사용자 기준으로 저장된 Refresh Token 제거
     */
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 토큰 재발급
     * - 전달된 Refresh Token의 유효성 검증(타입=RT, 만료, 서명)
     * - 저장된 토큰과 일치 여부 확인
     * - Access/Refresh Token 재발급 및 저장소 갱신
     */
    @Transactional
    public LoginResponse reissue(String refreshToken) {
        // ✅ AT/RT 혼동 방지를 위해 전용 검증 메서드 사용 (구 validateToken 사용 금지)
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // ✅ 전용 파서로 사용자 식별 (구 getUserId 사용 금지)
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        // 저장소에 보관 중인 RT 확인
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        // 전달된 RT와 저장된 RT 일치 검사
        if (!savedToken.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        // 신규 발급
        String newAccessToken = jwtTokenProvider.generateToken(userId, "USER");
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 저장소 갱신 (만료 역시 ms 단위 사용)
        savedToken.update(
                newRefreshToken,
                LocalDateTime.now().plus(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()))
        );
        refreshTokenRepository.save(savedToken);

        // 사용자 정보 조회 (응답 구성)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                user.getUserId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }
}
