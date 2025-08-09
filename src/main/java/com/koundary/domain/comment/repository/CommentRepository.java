package com.koundary.domain.comment.repository;

import com.koundary.domain.comment.entity.Comment;
<<<<<<< HEAD
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
>>>>>>> b1
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
<<<<<<< HEAD
    List<Comment> findAllByPost(Post post);
    List<Comment> findAllByUser(User user);
=======

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
>>>>>>> b1
}
