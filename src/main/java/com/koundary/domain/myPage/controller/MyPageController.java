package com.koundary.domain.myPage.controller;

import com.koundary.domain.myPage.dto.*;
import com.koundary.domain.myPage.service.MyPageService;
import com.koundary.domain.post.entity.Post;
import com.koundary.global.dto.PageResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/me")
    public ResponseEntity<MyPageProfileResponse> getMyPageProfile() {
         MyPageProfileResponse profile = myPageService.getMyPageProfile();
         return ResponseEntity.ok(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<MyPageMessageResponse> updatePassword(@RequestBody UpdatePasswordRequest request) {
        myPageService.updatePassword(request);
        return ResponseEntity.ok(new MyPageMessageResponse("비밀번호가 변경되었습니다."));
    }

    @PutMapping(
            value = "/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MyPageMessageResponse> updateProfileImage(@ModelAttribute UpdateProfileImageRequest request) {
        String url = myPageService.updateProfileImage(request);
        return ResponseEntity.ok(new MyPageMessageResponse("프로필 이미지가 변경되었습니다."));
    }

    @DeleteMapping("/delete-profile-image")
    public ResponseEntity<MyPageMessageResponse> deleteProfileImage() {
        myPageService.deleteProfileImage();
        return ResponseEntity.ok(new MyPageMessageResponse("프로필 이미지가 기본 이미지로 변경되었습니다."));
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<MyPageMessageResponse> deleteMyAccount() {
        myPageService.deleteMyAccount();
        return ResponseEntity.ok(new MyPageMessageResponse("회원 탈퇴가 완료되었습니다."));
    }

    @GetMapping("/scraps")
    public ResponseEntity<PageResponse<MyScrapItemResponse>> getMyScraps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = myPageService.getMyScraps(page, size);
        return ResponseEntity.ok(new PageResponse<>(result));
    }

    @GetMapping("/posts")
    public ResponseEntity<PageResponse<MyPostItemResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = myPageService.getMyPosts(page, size);
        return ResponseEntity.ok(new PageResponse<>(result));
    }

    @GetMapping("/commented-posts")
    public ResponseEntity<PageResponse<MyCommentedPostItemResponse>> getCommentedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = myPageService.getMyCommentedPosts(page, size);
        return ResponseEntity.ok(new PageResponse<>(result));
    }
}