package com.koundary.domain.myPage.service;

import com.koundary.domain.auth.service.PasswordPolicyService;
import com.koundary.domain.myPage.dto.UpdatePasswordRequest;
import com.koundary.domain.auth.repository.RefreshTokenRepository;
import com.koundary.domain.comment.repository.CommentRepository;
import com.koundary.domain.myPage.dto.*;
import com.koundary.domain.post.entity.Post;
import com.koundary.domain.post.repository.PostRepository;
import com.koundary.domain.scrap.repository.ScrapRepository;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.domain.verification.repository.VerificationRepository;
import com.koundary.global.security.CustomUserDetails;
import com.koundary.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final VerificationRepository verificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordPolicyService passwordPolicyService;

    @org.springframework.beans.factory.annotation.Value("${app.defaults.profile-image-url}")
    private String defaultProfileImageUrl;

    @Transactional(readOnly = true)
    public MyPageProfileResponse getMyPageProfile() {
        User me = getCurrentUser(); // 아래 헬퍼
        Long uid = me.getUserId();

        return MyPageProfileResponse.builder()
                .userId(uid)
                .loginId(me.getLoginId())
                .nickname(me.getNickname())
                .university(me.getUniversity())
                .universityEmail(me.getUniversityEmail())
                .nationality(me.getNationality())
                .profileImageUrl(me.getProfileImageUrl())
                .createdAt(me.getCreatedAt())
                .build();
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUser(); // 영속성 엔티티

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호, 확인 일치
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
        }

        // 새 비밀번호가 기존과 동일한지 검사
        passwordPolicyService.validateNotReused(user, request.getNewPassword());

        // 저장
        String encoded = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encoded);
        userRepository.save(user);
    }

    @Transactional
    public String updateProfileImage(UpdateProfileImageRequest request) {
        MultipartFile file = request.getProfileImage();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없습니다.");
        }

        String imageUrl = s3Uploader.upload(file, "profile");
        User user = getCurrentUser();
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
        return imageUrl;
    }

    @Transactional
    public void deleteProfileImage() {
        User user = getCurrentUser();
        String current = user.getProfileImageUrl();

        // 이미 기본 이미지면 종료
        if (current == null || current.equals(defaultProfileImageUrl)) {
            user.setProfileImageUrl(defaultProfileImageUrl);
            userRepository.save(user);
            return;
        }

        // URL -> key 추출해 S3 삭제
        try {
            String key = s3Uploader.keyFromUrl(current);
            s3Uploader.delete(key);
        } catch (Exception e) {
            // 로그만 남기고 계속 진행 (삭제 실패해도 프로필은 기본으로 전환)
        }

        // DB를 기본 이미지로 되돌림
        user.setProfileImageUrl(defaultProfileImageUrl);
        userRepository.save(user);
    }

    @Transactional
    public void deleteMyAccount() {
        User user = getCurrentUser();

        // 프로필 이미지가 기본이 아니면 S3에서 삭제
        String current = user.getProfileImageUrl();
        if (current == null || current.equals(defaultProfileImageUrl)) {
            try {
                String key = s3Uploader.keyFromUrl(current);
                s3Uploader.delete(key);
            } catch (Exception ignore) {
            }
        }

        // 이메일 인증 정보 삭제
        String email = user.getUniversityEmail();
        if (email != null && !email.isBlank()) {
            verificationRepository.deleteByEmail(email);
        }

        // 리프레시 토큰 삭제
        refreshTokenRepository.deleteByUserId(user.getUserId());

        // 유저 삭제
        userRepository.delete(user);

        // security context 삭제
        SecurityContextHolder.clearContext();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }

        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
    }

    // 스크랩한 글
    @Transactional(readOnly = true)
    public Page<MyScrapItemResponse> getMyScraps(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        User me =  getCurrentUser();

        return scrapRepository.findAllByUser(me, pageable)
                .map(s -> {
                    Post p = s.getPost();
                    var b = p.getBoard();
                    return MyScrapItemResponse.builder()
                            .postId(p.getPostId())
                            .title(p.getTitle())
                            .boardCode(b.getBoardCode())
                            .boardName(b.getBoardName())
                            .scrappedAt(s.getCreatedAt())
                            .build();
                });
    }

    // 내가 쓴 글
    @Transactional(readOnly = true)
    public Page<MyPostItemResponse> getMyPosts(int page, int size) {
        var  pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        User me =  getCurrentUser();

        return postRepository.findAllByUser(me, pageable)
                .map(p -> MyPostItemResponse.builder()
                        .postId(p.getPostId())
                        .title(p.getTitle())
                        .boardCode(p.getBoard().getBoardCode())
                        .boardName(p.getBoard().getBoardName())
                        .createdAt(p.getCreatedAt())
                        .build());
    }

    // 댓글 단 글
    @Transactional(readOnly = true)
    public Page<MyCommentedPostItemResponse> getMyCommentedPosts(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastAt"));
        User me = getCurrentUser();

        return commentRepository.findCommentedPostsWithLastTimeAndCount(me, pageable)
                .map(row -> {
                    Post p = row.getPost();
                    var b = p.getBoard();
                    return  MyCommentedPostItemResponse.builder()
                            .postId(p.getPostId())
                            .title(p.getTitle())
                            .boardCode(p.getBoard().getBoardCode())
                            .boardName(p.getBoard().getBoardName())
                            .lastCommentedAt(row.getLastAt())
                            .build();
                });
    }
}
