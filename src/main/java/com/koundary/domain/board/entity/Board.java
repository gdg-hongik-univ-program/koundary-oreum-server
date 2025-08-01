package com.koundary.domain.board.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId; // 게시판 고유 ID

    @Column(nullable = false, unique = true)
    private String boardCode; // 게시판 코드

    @Column(nullable = false)
    private String boardName; // 게시판 이름

    private String boardDescription; // 게시판 설명

    private int displayOrder; // 정렬 순서

    private boolean isWritable; // 글쓰기 가능 여부
}
