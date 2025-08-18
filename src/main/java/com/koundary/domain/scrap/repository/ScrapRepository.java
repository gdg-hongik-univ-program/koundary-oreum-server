package com.koundary.domain.scrap.repository;

import com.koundary.domain.myPage.dto.MyPostItemResponse;
import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    // 스크랩 여부 확인/조회
    Optional<Scrap> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);

    // 삭제
    void deleteByUserAndPost(User user, Post post);

    // 목록 조회
    List<Scrap> findAllByUser(User user);
    Page<Scrap> findAllByUser(User user, Pageable pageable);


    //쿼리로 DTO 생성
    @Query(
            value = """
        select new com.koundary.domain.myPage.dto.MyPostItemResponse(
          p.postId, p.title, b.boardCode, b.boardName, p.createdAt
        )
        from Scrap s
        join s.post p
        join p.board b
        where s.user.userId = :userId
        order by s.createdAt desc, s.id desc
      """,
            countQuery = """
        select count(s.id)
        from Scrap s
        where s.user.userId = :userId
      """
    )
    Page<MyPostItemResponse> findMyScraps(Long userId, Pageable pageable);

    @Query("""
        select new com.koundary.domain.myPage.dto.MyPostItemResponse(
          p.postId, p.title, b.boardCode, b.boardName, p.createdAt
        )
        from Scrap s
        join s.post p
        join p.board b
        where s.user.userId = :userId
        order by s.createdAt desc, s.id desc
    """)
    Slice<MyPostItemResponse> sliceMyScraps(Long userId, Pageable pageable);

}
