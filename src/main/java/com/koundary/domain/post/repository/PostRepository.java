package com.koundary.domain.post.repository;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 사용자별 최신순 전체 조회
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);

    // 특정 게시판 최신 3개
    List<Post> findTop3ByBoardOrderByCreatedAtDesc(Board board);

    // 사용자별 페이지 조회
    Page<Post> findAllByUser(User user, Pageable pageable);

    // 게시판별 전체 조회 (최신순)
    List<Post> findAllByBoardOrderByCreatedAtDesc(Board board);

    // ✅ 게시글 상세 (boardCode와 함께 안전하게 조회)
    @EntityGraph(attributePaths = {"images", "user"})
    Optional<Post> findByPostIdAndBoard_BoardCode(Long postId, String boardCode);

    // ✅ 게시판별 페이지 조회 (최신순)
    @Query("""
        select p
        from Post p
        join p.board b
        where b.boardCode = :boardCode
        order by p.createdAt desc
    """)
    Page<Post> findPageByBoardCode(@Param("boardCode") String boardCode, Pageable pageable);

    // ✅ groupKey로 두 글(원본/복사) 함께 가져오기
    @EntityGraph(attributePaths = {"images", "user", "board"})
    List<Post> findAllByGroupKey(String groupKey);

    @EntityGraph(attributePaths = {"board", "user", "images"})
    Page<Post> findByBoard_BoardCode(String boardCode, Pageable pageable);

    @EntityGraph(attributePaths = {"board", "user", "images"})
    Page<Post> findByBoard_BoardCodeAndUser_Nationality(String boardCode, String nationality, Pageable pageable);

    @EntityGraph(attributePaths = {"board", "user", "images"})
    Page<Post> findByBoard_BoardCodeAndUser_University(String boardCode, String university, Pageable pageable);

    // =========================================
    // ✅ 스크랩 수 증감 (단일 게시글)
    // =========================================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.scrapCount = p.scrapCount + :delta where p.postId = :postId")
    int updateScrapCount(@Param("postId") Long postId, @Param("delta") int delta);

    // =========================================
    // ✅ 스크랩 수 증감 (groupKey로 원본/복제 일괄 반영)
    // =========================================
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.scrapCount = p.scrapCount + :delta where p.groupKey = :groupKey")
    int updateScrapCountByGroupKey(@Param("groupKey") String groupKey, @Param("delta") int delta);
}
