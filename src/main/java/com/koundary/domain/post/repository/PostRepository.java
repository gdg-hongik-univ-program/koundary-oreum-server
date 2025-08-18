package com.koundary.domain.post.repository;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);
    List<Post> findTop3ByBoardOrderByCreatedAtDesc(Board board);

    Page<Post> findAllByUser(User user, PageRequest pageable);

    // ✅ 게시판별 게시글 전체 조회 (최신순)
    List<Post> findAllByBoardOrderByCreatedAtDesc(Board board);
}
