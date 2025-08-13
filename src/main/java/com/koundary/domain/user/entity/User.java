package com.koundary.domain.user.entity;


import com.koundary.domain.comment.entity.Comment;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.scrap.entity.Scrap;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    @Column(length = 50)
    private String nationality;

    @Column(length = 100)
    private String university;

    @Column(nullable = false, unique = true, length = 100)
    private String universityEmail;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(length = 300)
    private String profileImageUrl;

    @Builder
    public User(String loginId, String password, String nickname,
                String nationality, String university, String universityEmail, String profileImageUrl) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.nationality = nationality;
        this.university = university;
        this.universityEmail = universityEmail;
        this.profileImageUrl = profileImageUrl;
    }

    public void verifyEmail(){
        this.emailVerified = true;
    }

    public void updateProfileImage(String imageUrl){
        this.profileImageUrl = imageUrl;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Scrap> scraps = new ArrayList<>();
}