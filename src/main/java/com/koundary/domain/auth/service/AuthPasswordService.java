package com.koundary.domain.auth.service;

import com.koundary.domain.auth.entity.PasswordResetToken;
import com.koundary.domain.auth.repository.PasswordResetTokenRepository;
import com.koundary.domain.auth.repository.RefreshTokenRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.global.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthPasswordService {

    private static final int EXPIRE_MIN = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;

    /** 비밀번호 재설정 토큰 발급 */
    @Transactional
    public void issuePasswordResetToken(String universityEmail, String ip, String ua) {
        userRepository.findByUniversityEmail(universityEmail).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getUserId());
            String raw = TokenUtil.generate();
            String hash = TokenUtil.sha256Base64(raw);
            PasswordResetToken token = PasswordResetToken.issue(user.getUserId(), hash, EXPIRE_MIN, ip, ua);
            passwordResetTokenRepository.save(token);

            String link = "https://your-frontend/reset-password?token=" +raw;
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(universityEmail);
            message.setSubject("[Koundary] 비밀번호 재설정");
            message.setText("아래 링크로 새 비밀번호를 설정하세요: \n" + link);

            mailSender.send(message);
        });
        // 계정 존재 여부와 관계없이 동일한 응답을 주도록 컨트롤러에서 메시지 고정
    }

    /** 토큰으로 새 비밀번호 설정 */
    @Transactional
    public void resetPasswordWithToken(String rawToken, String newPw, String confirmPw) {
        if (newPw == null || newPw.equals(confirmPw))
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        String hash = TokenUtil.sha256Base64(rawToken);
        var token = passwordResetTokenRepository.findValidByHash(hash, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));
        User user = userRepository.findById(token.getUserId()).orElseThrow();
        applyNewPassword(user, newPw);
        token.markUsed();
        // flush는 트랜잭션 종료 시점에 반영
    }

    private void applyNewPassword(User user, String rawNewPw) {
        user.setPassword(passwordEncoder.encode(rawNewPw));
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getUserId());
    }

    @Transactional(readOnly = true)
    public void sendLoginIdByEmail(String universityEmail) {
        userRepository.findByUniversityEmail(universityEmail).ifPresent(user -> {
            String loginId = user.getLoginId();
            String body = """
                    요청하신 계정의 로그인 아이디는 다음과 같습니다:

                    %s

                    감사합니다.
                    """.formatted(loginId);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(universityEmail);
            message.setSubject("[Koundary] 아이디 안내");
            message.setText(body);

            mailSender.send(message);
        });
        // 계정 존재 여부와 상관없이 컨트롤러 응답은 동일해야 함
    }
}
