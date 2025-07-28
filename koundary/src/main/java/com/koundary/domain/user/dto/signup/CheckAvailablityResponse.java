package com.koundary.domain.user.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckAvailablityResponse {

    private boolean availablity;

    private String message;
}
