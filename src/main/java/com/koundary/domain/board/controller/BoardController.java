package com.koundary.domain.board.controller;

import com.koundary.domain.board.dto.BoardResponse;
import com.koundary.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping(produces = "application/json; charset=UTF-8")
    public List<BoardResponse> getBoards() {
        return boardService.getAllBoards();
    }
}
