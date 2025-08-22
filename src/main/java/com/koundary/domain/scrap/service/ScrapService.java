package com.koundary.domain.scrap.service;

import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.scrap.repository.ScrapRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginId;
        Object principal = auth != null ? auth.getPrincipal() : null;

        if (principal instanceof UserDetails ud) {
            loginId = ud.getUsername();
        } else if (principal instanceof String s) {
            loginId = s;
        } else {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다. loginId=" + loginId));
    }

    /**
     * 스크랩 토글
     * - 현재 스크랩이 있으면 삭제하고 scrapCount -1
     * - 현재 스크랩이 없으면 생성하고 scrapCount +1
     * - groupKey가 있으면 원본/복제글 모두 동기 반영
     *
     * @return true  -> 토글 후 '스크랩됨'
     *         false -> 토글 후 '스크랩 해제됨'
     */
    @Transactional
    public boolean toggleScrap(Long postId) {
        User user = getCurrentUser();
        Long userId = user.getUserId();

        // 게시글 조회 (groupKey 필요)
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 현재 스크랩 상태 조회
        Optional<Scrap> existing = scrapRepository.findByPost_PostIdAndUser_UserId(postId, userId);

        if (existing.isPresent()) {
            // 이미 스크랩 → 삭제
            scrapRepository.delete(existing.get());

            if (post.getGroupKey() != null) {
                postRepository.updateScrapCountByGroupKey(post.getGroupKey(), -1);
            } else {
                postRepository.updateScrapCount(postId, -1);
            }
            return false; // 현재 상태: 해제됨
        } else {
            // 스크랩 추가
            try {
                Scrap scrap = Scrap.builder()
                        .user(user)
                        .post(post)
                        .createdAt(LocalDateTime.now())
                        .build();
                scrapRepository.save(scrap);
            } catch (DataIntegrityViolationException e) {
                // 드물게 동시성으로 인해 유니크 충돌 시: 이미 생성된 것으로 간주하고 증가로 일관
            }

            if (post.getGroupKey() != null) {
                postRepository.updateScrapCountByGroupKey(post.getGroupKey(), 1);
            } else {
                postRepository.updateScrapCount(postId, 1);
            }
            return true; // 현재 상태: 스크랩됨
        }
    }
}
