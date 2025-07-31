package com.koundary.domain.verification.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Verification {

    @Id
    private String email;

    private String code;

    private boolean Verified;

    private LocalDateTime expiresAt;

    private LocalDateTime lastSentAt;

    public void verify(String inputCode) {
        if (!this.code.equals(inputCode)) throw new IllegalArgumentException("인증코드가 일치하지 않습니다.");
        if (expiresAt.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("인증코드가 만료되었습니다.");
        this.Verified = true;
    }
}
