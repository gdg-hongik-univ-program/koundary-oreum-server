package com.koundary.domain.myPage.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class UpdateProfileImageRequest {
    private MultipartFile profileImage;
}
