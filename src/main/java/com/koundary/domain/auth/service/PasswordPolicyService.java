package com.koundary.domain.auth.service;

import com.koundary.domain.auth.entity.PasswordHistory;
import com.koundary.domain.auth.repository.PasswordHistoryRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.global.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    // 정책 파라미터
    private static final int HISTORY_CHECK_SIZE = 5; // 최근 N개와 비교
    private static final int HISTORY_MAX_KEEP  = 5; // 보관 최대 개수

    /** 새 비밀번호가 최근 이력과 겹치지 않는지 검사 */
    @Transactional(readOnly = true)
    public void validateNotReused(User user, String rawNewPassword) {
        var recent = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(0, HISTORY_CHECK_SIZE)
        );
        for (PasswordHistory h : recent) {
            if (passwordEncoder.matches(rawNewPassword, h.getPasswordHash())) {
                throw new BadRequestException("이전에 사용한 비밀번호는 사용할 수 없습니다.");
            }
        }
    }

    /** 변경된 비밀번호 해시를 이력에 적재하고 초과분은 정리 */
    @Transactional
    public void recordNewPassword(User user, String encodedNewPassword) {
        passwordHistoryRepository.save(
                PasswordHistory.builder()
                        .user(user)
                        .passwordHash(encodedNewPassword)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        List<PasswordHistory> all = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        if (all.size() > HISTORY_MAX_KEEP) {
            all.stream().skip(HISTORY_MAX_KEEP).forEach(passwordHistoryRepository::delete);
        }
    }
}
