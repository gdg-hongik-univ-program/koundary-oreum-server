package com.koundary.domain.post.repository;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);
    List<Post> findTop3ByBoardOrderByCreatedAtDesc(Board board);
}
