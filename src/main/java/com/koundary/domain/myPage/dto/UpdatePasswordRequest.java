package com.koundary.domain.myPage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePasswordRequest {
    private String currentPassword;
    private String newPassword;
}
