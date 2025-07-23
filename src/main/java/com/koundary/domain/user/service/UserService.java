package com.koundary.domain.user.service;

import com.koundary.domain.user.dto.signup.CheckAvailablityResponse;
import com.koundary.domain.user.dto.signup.CheckLoginIdRequest;
import com.koundary.domain.user.dto.signup.CheckNicknameRequest;
import com.koundary.domain.user.dto.signup.SignupRequest;
import com.koundary.domain.user.entity.User;
import com.koundary.domain.user.repository.UserRepository;
import com.koundary.domain.verification.service.VerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService VerificationService;

    @Transactional
    public void signup(SignupRequest signupRequest) {

        if (userRepository.existsByLoginId(signupRequest.getLoginId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다.");
        }

        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        if (userRepository.existsByUniversityEmail(signupRequest.getUniversityEmail())){
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        boolean isVerified = VerificationService.isVerified(signupRequest.getUniversityEmail());
        if(!isVerified){
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.builder()
                .loginId(signupRequest.getLoginId())
                .password(encodedPassword)
                .nickname(signupRequest.getNickname())
                .university(signupRequest.getUniversityEmail())
                .university(signupRequest.getUniversity())
                .nationality(signupRequest.getNationality())
                .build();

        userRepository.save(user);
    }

    public CheckAvailablityResponse checkLoginIdDuplicate(CheckLoginIdRequest dto) {
        boolean exists = userRepository.existsByLoginId(dto.getLoginId());
        return exists
                ? new CheckAvailablityResponse(false, "이미 사용중인 아이디입니다.")
                : new CheckAvailablityResponse(true, "사용 가능한 아이디입니다.");
    }

    public CheckAvailablityResponse checkNicknameDuplicate(CheckNicknameRequest dto) {
        boolean exists = userRepository.existsByNickname(dto.getNickname());
        return exists
                ? new CheckAvailablityResponse(false, "이미 사용중인 닉네임입니다.")
                : new CheckAvailablityResponse(true, "사용 가능한 닉네임입니다.");
    }
}
