package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "국가는 필수 선택 항목입니다.")
    private String nationality;

    @NotBlank(message = "대학교는 필수 선택 항목입니다.")
    private String university;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    private String nickname;

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;

    @NotBlank(message = "학교 메일은 필수 입력 항목입니다.")
    private String universityEmail;
}
