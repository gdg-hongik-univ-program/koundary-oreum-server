package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.NotBlank;

public class CheckLoginIDRequest {

    @NotBlank
    private String loginID;

    public CheckLoginIDRequest() {}

    public CheckLoginIDRequest(@NotBlank String loginID) {
        this.loginID = loginID;
    }

    public String getLoginID() {
        return loginID;
    }

    public void setLoginID(String loginID) {
        this.loginID = loginID;
    }
}
