package com.koundary.domain.scrap.service;

import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.scrap.dto.ScrapResponse;
import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.scrap.repository.ScrapRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /** 스크랩 추가 */
    public void addScrap(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (scrapRepository.existsByUserAndPost(user, post)) {
            return; // 이미 스크랩되어 있으면 조용히 종료 (또는 예외로 처리해도 됨)
        }
        Scrap scrap = Scrap.builder()
                .user(user)
                .post(post)
                .build();
        scrapRepository.save(scrap);
    }

    /** 스크랩 삭제 */
    public void removeScrap(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        scrapRepository.deleteByUserAndPost(user, post);
    }

    /** 스크랩 토글 */
    public ScrapResponse toggleScrap(Long postId) {
        User user = getCurrentUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return scrapRepository.findByUserAndPost(user, post)
                .map(existing -> {
                    scrapRepository.delete(existing);
                    return new ScrapResponse(false);
                })
                .orElseGet(() -> {
                    scrapRepository.save(Scrap.builder().user(user).post(post).build());
                    return new ScrapResponse(true);
                });
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
