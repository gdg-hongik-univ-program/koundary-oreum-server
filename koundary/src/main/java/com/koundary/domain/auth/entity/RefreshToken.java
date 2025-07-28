package com.koundary.domain.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * RefreshToken entity
 */

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiry;

    public RefreshToken() {}

    /**
     *
     * @param userId 사용자 ID
     * @param token 저장할 RefreshToken
     * @param expiry 토큰 만료 시간
     */

    public RefreshToken(Long userId, String token, LocalDateTime expiry) {
        this.userId = userId;
        this.token = token;
        this.expiry = expiry;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}
