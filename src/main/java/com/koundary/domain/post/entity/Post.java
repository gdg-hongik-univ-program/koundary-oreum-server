package com.koundary.domain.post.entity;


import com.koundary.domain.board.entity.Board;
import com.koundary.domain.language.entity.Language;
import com.koundary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean isInformation;

    //게시판
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    //작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //언어
    /*
    @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "language_id")
     private Language language;
     */

    //이미지
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /*
    public void setLanguage(Language language) {
        this.language = language;
    }
     */
}
