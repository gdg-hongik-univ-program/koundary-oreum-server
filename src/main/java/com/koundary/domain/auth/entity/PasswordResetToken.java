package com.koundary.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token", indexes = {
        @Index(name = "idx_prt_token_hash", columnList = "tokenHash", unique = true),
        @Index(name = "idx_prt_login_id", columnList = "login_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    private String issuedIp;
    @Column(length = 512)
    private String issuedUa;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public static PasswordResetToken issue(Long userId, String tokenHash, int expireMinutes, String ip, String ua) {
        return PasswordResetToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(expireMinutes))
                .used(false)
                .issuedIp(ip)
                .issuedUa(ua)
                .build();
    }

    public void markUsed() {
        this.used = true;
    }
}
