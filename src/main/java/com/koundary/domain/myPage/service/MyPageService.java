package com.koundary.domain.myPage.service;

import com.koundary.domain.comment.repository.CommentRepository;
import com.koundary.domain.myPage.dto.UpdatePasswordRequest;
import com.koundary.domain.myPage.dto.UpdateProfileImageRequest;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.scrap.repository.ScrapRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ScrapRepository scrapRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Uploader s3Uploader;

    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void updateProfileImage(UpdateProfileImageRequest request) {
        MultipartFile file = request.getProfileImage();
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다.");
        }

        String imageUrl = s3Uploader.upload(file, "profile");
        User user = getCurrentUser();
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
    }

    public void deleteMyAccount() {
        User user = getCurrentUser();
        userRepository.delete(user);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
