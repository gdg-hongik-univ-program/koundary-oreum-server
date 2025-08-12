package com.koundary.domain.myPage.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPageProfileResponse {
    private Long userId;
    private String loginId;
    private String nickname;
    private String university;
    private String universityEmail;
    private String nationality;
    private String profileImageUrl;
    private boolean defaultProfileImage; //기본 이미지 여부
    private long postCount;
    private long commentCount;
    private String createdAt;
}