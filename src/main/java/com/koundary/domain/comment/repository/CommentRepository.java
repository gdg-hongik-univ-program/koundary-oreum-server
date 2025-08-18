package com.koundary.domain.comment.repository;

import com.koundary.domain.comment.entity.Comment;
import com.koundary.domain.myPage.dto.MyPostItemResponse;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 최상위 댓글 페이지 조회
    @EntityGraph(attributePaths = {"author"})
    Page<Comment> findByPost_PostIdAndParentIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);

    // 특정 댓글의 대댓글 목록
    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByParent_IdOrderByCreatedAtAsc(Long parentId);

    // 대댓글 개수
    int countByParent_Id(Long parentId);

    // 게시글 전체 댓글 수 (소프트삭제 제외)
    int countByPost_PostIdAndDeletedFalse(Long postId);

    long countByUserAndPost(User author, Post post);

    @Query(
            value = """
        select distinct new com.koundary.domain.myPage.dto.MyPostItemResponse(
          p.postId, p.title, b.boardCode, b.boardName, p.createdAt
        )
        from Comment c
        join c.post p
        join p.board b
        where c.user.userId = :userId
        order by p.createdAt desc, p.postId desc
      """,
            countQuery = """
        select count(distinct p.postId)
        from Comment c
        join c.post p
        where c.user.userId = :userId
      """
    )
    Page<MyPostItemResponse> findMyCommentedPosts(Long userId, Pageable pageable);

    @Query("""
        select distinct new com.koundary.domain.myPage.dto.MyPostItemResponse(
          p.postId, p.title, b.boardCode, b.boardName, p.createdAt
        )
        from Comment c
        join c.post p
        join p.board b
        where c.user.userId = :userId
        order by p.createdAt desc, p.postId desc
    """)
    Slice<MyPostItemResponse> sliceMyCommentedPosts(Long userId, Pageable pageable);

    interface CommentedPostProjection {
        Post getPost();
        java.time.LocalDateTime getLastAt();
        Long getCnt();
    }
}
