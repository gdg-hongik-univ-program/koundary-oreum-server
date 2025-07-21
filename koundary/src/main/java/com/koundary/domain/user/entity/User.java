package com.koundary.domain.user.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "loginId"),
        @UniqueConstraint(columnNames = "nickname"),
        @UniqueConstraint(columnNames = "universityEmail")
} )

@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(length = 50)
    private String nationality;

    @Column(length = 100)
    private String university;

    @Column(nullable = false, length = 100)
    private String universityEmail;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(length = 300)
    private String profileImage;

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNationality() {
        return nationality;
    }

    public String getUniversity() {
        return university;
    }

    public String getUniversityEmail() {
        return universityEmail;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void verifyEmail(){
        this.emailVerified = true;
    }

    public void updateProfileImage(String imageUrl){
        this.profileImage = imageUrl;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }
}