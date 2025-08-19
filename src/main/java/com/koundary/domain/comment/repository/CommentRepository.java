package com.koundary.domain.comment.repository;

import com.koundary.domain.comment.entity.Comment;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 최상위 댓글 페이지 조회
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);

    // 특정 댓글의 대댓글 목록
    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByParent_IdOrderByCreatedAtAsc(Long parentId);

    // 댓글 단 글 조회
    @EntityGraph(attributePaths = {"post", "post.board"})
    Page<Comment> findAllByUser(User user, Pageable pageable);

    // 대댓글 개수
    int countByParent_Id(Long parentId);

    // 게시글 전체 댓글 수 (소프트삭제 제외)
    int countByPost_PostIdAndDeletedFalse(Long postId);

    long countByUserAndPost(User user, Post post);

    @Query("""
           select c.post as post,
                  max(c.createdAt) as lastAt,
                  count(c) as cnt
           from Comment c
           where c.user = :user
           group by c.post
           order by max(c.createdAt) desc
           """)
    Page<CommentRepository.CommentedPostProjection> findCommentedPostsWithLastTimeAndCount(
            @Param("user") User user, Pageable pageable
    );

    interface CommentedPostProjection {
        Post getPost();
        java.time.LocalDateTime getLastAt();
        Long getCnt();
    }
}
