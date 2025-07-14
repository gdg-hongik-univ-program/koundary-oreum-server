package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CheckNicknameRequest {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    private String nickname;
}
