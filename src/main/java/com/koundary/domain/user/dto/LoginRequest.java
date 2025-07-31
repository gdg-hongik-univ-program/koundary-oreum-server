package com.koundary.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 정보를 담는 DTO
 */
public class LoginRequest {
    @NotBlank(message = "아이디를 입력해 주세요.")
    private String loginId;

    @NotBlank(message =  "비밀번호를 입력해 주세요")
    private String password;

    public LoginRequest() {

    }

    public LoginRequest(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
