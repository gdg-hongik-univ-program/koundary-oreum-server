package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.NotBlank;

public class CheckNicknameRequest {
    @NotBlank
    private String nickname;

    public CheckNicknameRequest() {}

    public CheckNicknameRequest(@NotBlank String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
