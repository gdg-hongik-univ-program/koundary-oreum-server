package com.koundary.domain.board.dto;

import com.koundary.domain.board.entity.Board;
import lombok.Builder;

public record BoardResponse(
        Long boardId,
        String boardCode,
        String boardName,
        String boardDescription,
        int displayOrder,
        boolean isWritable
) {
    public BoardResponse {}

    // Entity → DTO 변환 메서드
    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getBoardId(),
                board.getBoardCode(),
                board.getBoardName(),
                board.getBoardDescription(),
                board.getDisplayOrder(),
                board.isWritable()
        );
    }
}
