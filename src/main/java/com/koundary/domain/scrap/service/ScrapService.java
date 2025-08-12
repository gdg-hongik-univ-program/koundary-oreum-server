package com.koundary.domain.scrap.service;

import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.myPage.dto.MyScrapItemResponse;
import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.scrap.exception.DuplicateScrapException;
import com.koundary.domain.scrap.repository.ScrapRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다." + loginId));
    }

    @Transactional
    public void addScrap(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        if (scrapRepository.existsByUserAndPost(user, post)) {
            throw new DuplicateScrapException("이미 스크랩한 게시글입니다.");
        }

        try {
            Scrap scrap = Scrap.builder()
                    .user(user)
                    .post(post)
                    .createdAt(LocalDateTime.now())
                    .build();
            scrapRepository.save(scrap);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateScrapException("이미 스크랩한 게시글입니다.");
        }
    }

    @Transactional
    public void removeScrap(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        scrapRepository.findByUserAndPost(user, post).ifPresent(scrapRepository::delete);
    }
}
