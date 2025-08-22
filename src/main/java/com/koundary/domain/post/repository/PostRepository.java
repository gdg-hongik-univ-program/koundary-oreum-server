package com.koundary.domain.post.repository;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);
    List<Post> findTop3ByBoardOrderByCreatedAtDesc(Board board);

    @EntityGraph(attributePaths = {"board"})
    Page<Post> findAllByUser(User user, PageRequest pageable);

    // ✅ 게시판별 게시글 전체 조회 (최신순)
    List<Post> findAllByBoardOrderByCreatedAtDesc(Board board);

    // 국가별: Board.boardCode = "NATIONALITY" && 작성자 User.nationality = {me.nationality}
    @EntityGraph(attributePaths = {"board", "user", "images"})
    Page<Post> findByBoard_BoardCodeAndUser_Nationality(
            String boardCode,
            String nationality,
            Pageable pageable
    );

    // 학교별: Board.boardCode = "UNIVERSITY" && 작성자 User.university = {me.university}
    @EntityGraph(attributePaths = {"board", "user", "images"})
    Page<Post> findByBoard_BoardCodeAndUser_University(
            String boardCode,
            String university,
            Pageable pageable
    );

    // 특정 게시판 전체 (목록)
    @EntityGraph(attributePaths = {"board", "user", "images"})
    List<Post> findAllByBoard_BoardCode(String boardCode, Sort sort);

    // 내 나라 사람들 글 (NATIONALITY 게시판 + 작성자.nationality == me.nationality)
    @EntityGraph(attributePaths = {"board", "user", "images"})
    List<Post> findAllByBoard_BoardCodeAndUser_Nationality(String boardCode, String nationality, Sort sort);

    // 내 학교 사람들 글 (UNIVERSITY 게시판 + 작성자.university == me.university)
    @EntityGraph(attributePaths = {"board", "user", "images"})
    List<Post> findAllByBoard_BoardCodeAndUser_University(String boardCode, String university, Sort sort);
}