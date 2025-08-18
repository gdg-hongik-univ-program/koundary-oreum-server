package com.koundary.domain.post.repository;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.myPage.dto.MyPostItemResponse;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByUserOrderByCreatedAtDesc(User user);
    List<Post> findTop3ByBoardOrderByCreatedAtDesc(Board board);


    //쿼리로 DTO 뽑아서 쓰기
    @Query(
            value = """
        select new com.koundary.domain.myPage.dto.MyPostItemResponse(
          p.postId, p.title, b.boardCode, b.boardName, p.createdAt
        )
        from Post p
        join p.board b
        where p.user.userId = :userId
        order by p.createdAt desc, p.postId desc
      """,
            countQuery = """
        select count(p.postId)
        from Post p
        where p.user.userId = :userId
      """
    )
    Page<MyPostItemResponse> findMyPosts(Long userId, Pageable pageable);

    // 무한스크롤(더보기)용: count 생략으로 가볍게
    @Query("""
        select new com.koundary.domain.myPage.dto.MyPostItemResponse(
          p.postId, p.title, b.boardCode, b.boardName, p.createdAt
        )
        from Post p
        join p.board b
        where p.user.userId = :userId
        order by p.createdAt desc, p.postId desc
    """)
    Slice<MyPostItemResponse> sliceMyPosts(Long userId, Pageable pageable);
}
