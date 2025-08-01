package com.koundary.domain.board.repository;

import com.koundary.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByBoardCode(String boardCode);
    List<Board> findAllByOrderByDisplayOrderAsc();
}
