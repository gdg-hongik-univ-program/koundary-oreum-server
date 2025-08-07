package com.koundary.domain.scrap.repository;

import com.koundary.domain.post.entity.Post;
import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // 내가 스크랩한 모든 글
    List<Scrap> findAllByUser(User user);

    // 스크랩 여부 확인 (토글용)
    Optional<Scrap> findByUserAndPost(User user, Post post);

    // 스크랩 삭제
    void deleteByUserAndPost(User user, Post post);
}
