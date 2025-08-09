package com.koundary.domain.verification.service;


import com.koundary.domain.verification.entity.Verification;
import com.koundary.domain.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final JavaMailSender javaMailSender;

    public void sendVerificationCode(String email) {
        Verification target;

        Optional<Verification> optionalVerification = verificationRepository.findByEmail(email);

        if (optionalVerification.isPresent()) {
            Verification existing = optionalVerification.get();

            if (existing.getLastSentAt() != null &&
                    existing.getLastSentAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                throw new IllegalArgumentException("인증 코드는 1분 후에 다시 요청할 수 있습니다.");
            }

            existing.setCode(generateCode());
            existing.setVerified(false);
            existing.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            existing.setLastSentAt(LocalDateTime.now());

            target = verificationRepository.save(existing);
        } else {
            Verification newEntity = new Verification();
            newEntity.setEmail(email);
            newEntity.setCode(generateCode());
            newEntity.setVerified(false);
            newEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            newEntity.setLastSentAt(LocalDateTime.now());

            target = verificationRepository.save(newEntity);
        }

        sendEmail(email, target.getCode());
    }


    public void verifyCode(String email, String inputCode) {
        Verification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청이 없습니다."));
        verification.verify(inputCode);
        verificationRepository.save(verification);
    }

    public boolean isVerified(String email) {
        return verificationRepository.findByEmail(email)
                .map(Verification::isVerified)
                .orElse(false);
    }

    private String generateCode() {
        return String.valueOf((int)(Math.random()*900000+100000)); //6자리 숫자
    }

    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Koundary 이메일 인증코드");
        message.setText("인증코드는 다음과 같습니다: "+code);
        javaMailSender.send(message);
    }
}