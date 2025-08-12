package com.koundary.domain.verification.repository;

import com.koundary.domain.verification.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, String> {
    Optional<Verification> findByEmail(String email);

    void deleteByEmail(String email);
}
