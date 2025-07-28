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
    private final UserRepository userRepository; //사용자 정보 조회 Repository
    private final RefreshTokenRepository refreshTokenRepository; // RefreshToken
    private final JwtTokenProvider jwtTokenProvider; // JWT 생성
    private final PasswordEncoder passwordEncoder; //비밀번호 암호와 및 비교

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     *
     * @param request 로그인 요청(아이디, 비밀번호)
     * @return 로그인 성공 시 응답 객체 반환
     */

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // loginId로 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

        // 비밀번호 일치 여부 확인
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //JWT access,refreshToken 발급
        String accessToken = jwtTokenProvider.generateToken(user.getUserId(), "USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());


        //사용자가 다시 로그인 할 때 기존 refreshToken 삭제 후 새로 저장
        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.save(new RefreshToken(user.getUserId(),
                refreshToken,
                LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration())
                )
        );

        // 로그인 응답 객체 반환
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUserId(),
                user.getNickname(),
                user.getProfileImage()
        );
    }
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
