package com.koundary.domain.verification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailVerifyRequest {
    private String email;
    private String code;
}
