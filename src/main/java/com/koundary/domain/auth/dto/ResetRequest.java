package com.koundary.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResetRequest {

    @NotBlank
    private String loginId;

    @Email @NotBlank
    private String universityEmail;
}
