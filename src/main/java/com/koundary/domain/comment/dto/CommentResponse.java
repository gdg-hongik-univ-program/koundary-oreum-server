package com.koundary.domain.comment.dto;

<<<<<<< HEAD
import com.koundary.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String authorNickname;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorNickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
=======
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long commentId;
    private Long postId;

    // 작성자 정보 (익명 정책 사용 시 nickname만 활용)
    private Long authorId;
    private String authorNickname;
    private String authorProfileImage;

    private String content;
    private boolean deleted;   // 소프트 삭제 여부
    private boolean mine;      // 내가 쓴 댓글인지(프론트에서 수정/삭제 버튼 제어)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 대댓글
    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

    // 대댓글 개수 (목록에서 “더보기” 버튼 표시용)
    private Integer replyCount;
>>>>>>> b1
}
