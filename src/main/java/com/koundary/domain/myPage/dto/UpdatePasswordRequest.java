package com.koundary.domain.myPage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank(message = "현재 비밀번호를 입력해주세요")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
    private String confirmNewPassword;
}
