package com.koundary.domain.myPage.controller;

import com.koundary.domain.myPage.dto.UpdatePasswordRequest;
import com.koundary.domain.myPage.dto.UpdateProfileImageRequest;
import com.koundary.domain.myPage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
        myPageService.updatePassword(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile-image")
    public ResponseEntity<Void> updateProfileImage(@ModelAttribute UpdateProfileImageRequest request) {
        myPageService.updateProfileImage(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount() {
        myPageService.deleteMyAccount();
        return ResponseEntity.noContent().build();
    }
}