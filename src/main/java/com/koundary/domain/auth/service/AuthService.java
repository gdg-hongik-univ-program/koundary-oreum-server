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

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getUserId(), "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.save(new RefreshToken(
                user.getUserId(),
                refreshToken,
                LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration())
        ));

        return new LoginResponse(
                accessToken, refreshToken, user.getUserId(), user.getNickname(), user.getProfileImageUrl()
        );
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public LoginResponse reissue(String refreshToken) {
        // ✅ Refresh 전용 검증 사용
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        if (!savedToken.getToken().equals(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.generateToken(userId, "USER");
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        savedToken.update(newRefreshToken,
                LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration()));
        refreshTokenRepository.save(savedToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        return new LoginResponse(
                newAccessToken, newRefreshToken, user.getUserId(), user.getNickname(), user.getProfileImageUrl()
        );
    }
}
