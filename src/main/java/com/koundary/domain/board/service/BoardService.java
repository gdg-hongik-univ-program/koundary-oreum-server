package com.koundary.domain.board.service;

import com.koundary.domain.board.dto.BoardResponse;
import com.koundary.domain.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(BoardResponse::from)
                .toList();
    }
}
