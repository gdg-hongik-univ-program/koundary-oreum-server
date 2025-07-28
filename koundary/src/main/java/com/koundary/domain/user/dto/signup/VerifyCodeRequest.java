package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email
    private String email;

    @NotBlank(message = "인증코드는 필수 입력 항목입니다.")
    private String code;
}
