package com.koundary.domain.scrap.repository;

import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // 스크랩 여부 확인/조회
    Optional<Scrap> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);

    Optional<Scrap> findByPost_PostIdAndUser_UserId(Long postId, Long userId);
    boolean existsByPost_PostIdAndUser_UserId(Long postId, Long userId);

    // 삭제
    void deleteByUserAndPost(User user, Post post);
    void deleteByPost_PostIdAndUser_UserId(Long postId, Long userId);

    // 목록 조회
    @EntityGraph(attributePaths = {"post", "post.board"})
    Page<Scrap> findAllByUser(User user, Pageable pageable);

    @Query("select s.post.postId from Scrap s where s.user.userId = :userId and s.post.postId in :postIds")
    List<Long> findScrappedPostIdsByUserAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

}
