package com.koundary.domain.auth.repository;

import com.koundary.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * RefreshToken entity를 위한 JPA Repository
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);

    /** 사용자 전 기기 로그안웃 효과 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken r where r.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}