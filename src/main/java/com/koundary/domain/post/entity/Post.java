package com.koundary.domain.post.entity;

import com.koundary.domain.board.entity.Board;
import com.koundary.domain.scrap.entity.Scrap;
import com.koundary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // ✅ 원본/복사본을 묶는 키 (둘 다 동일한 값)
    @Column(length = 36)
    private String groupKey;

    // ✅ 스크랩 수 (기본값 0)
    @Column(nullable = false)
    private int scrapCount = 0;

    // ✅ 낙관적 잠금(동시성 보호)
    @Version
    private Long version;


    // 게시판
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 이미지
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // 글이 지워질 때 해당 글을 스크랩한 레코드 제거
    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Scrap> scraps = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //언어
    /*
    @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "language_id")
     private Language language;
     */

    /*
    public void setLanguage(Language language) {
        this.language = language;
    }
     */

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setGroupKey(String groupKey) { this.groupKey = groupKey; }

    // ✅ 업데이트용 편의 메서드
    public void updateContent(String title, String content, boolean isInformation) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        this.isInformation = isInformation;
    }

    public void replaceImages(List<Image> newImages) {
        this.images.clear();
        if (newImages != null) {
            this.images.addAll(newImages);
        }
    }

    // ✅ 스크랩 수 증감 편의 메서드
    public void increaseScrapCount() {
        this.scrapCount++;
    }

    public void decreaseScrapCount() {
        if (this.scrapCount > 0) {
            this.scrapCount--;
        }
    }
}
