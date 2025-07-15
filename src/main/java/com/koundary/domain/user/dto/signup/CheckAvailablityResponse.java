package com.koundary.domain.user.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckAvailablityResponse {

    private boolean availablity;

    private String message;
}
