package com.koundary.domain.user.dto;

/**
 * 로그인 성공 시 클라이언트에게 전달되는 응답 DTO
 */
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String nickname;
    private String profileImage;


    public LoginResponse() {

    }
    public LoginResponse(String accessToken,String refreshToken, Long userId, String nickname, String profileImage) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
