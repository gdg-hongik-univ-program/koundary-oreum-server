package com.koundary.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public class LogoutRequest {

    @NotNull(message = "userId는 필수입니다.")
    private Long userId;

    public LogoutRequest() {}

    public LogoutRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
