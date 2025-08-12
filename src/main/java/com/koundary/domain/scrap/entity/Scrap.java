package com.koundary.domain.scrap.entity;

import com.koundary.domain.post.entity.Post;
import com.koundary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "scrap",
        uniqueConstraints = @UniqueConstraint(name = "uk_scrap_user_post", columnNames = {"user_id", "post_id"})
)
public class Scrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 스크랩한 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 스크랩 대상 게시글
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 스크랩 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
