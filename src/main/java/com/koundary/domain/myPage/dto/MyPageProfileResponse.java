package com.koundary.domain.myPage.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPageProfileResponse {
    private String nickname;
    private String universityEmail;
    private String profileImageUrl;
}
