package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.NotBlank;

public class SignupRequest {

    @NotBlank
    private String nationality;

    @NotBlank
    private String university;

    @NotBlank
    private String nickname;

    @NotBlank
    private String loginID;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    private String universityEmail;

    @NotBlank
    private String emailVerificationCode;

    public SignupRequest() {}

}
