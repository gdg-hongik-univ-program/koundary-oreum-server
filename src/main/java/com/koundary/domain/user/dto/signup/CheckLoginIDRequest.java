package com.koundary.domain.user.dto.signup;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckLoginIDRequest {

    @NotBlank(message = "ID는 필수 입력 항목입니다.")
    private String loginId;
}
