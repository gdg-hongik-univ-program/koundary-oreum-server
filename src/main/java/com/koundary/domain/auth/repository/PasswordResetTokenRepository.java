package com.koundary.domain.auth.repository;

import com.koundary.domain.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 비밀번호 재설정 토큰 저장소
 * 원문 토큰은 저장하지 않고 해시만 저장함
 */
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {

    /** 해시 값으로 토큰 단건 조회 */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * 유효한(미사용, 미만료) 토큰 조회
     */
    @Query("""
           select t
             from PasswordResetToken t
            where t.tokenHash = :hash
              and t.used = false
              and t.expiresAt > :now
           """)
    Optional<PasswordResetToken> findValidByHash(@Param("hash") String tokenHash,
                                                 @Param("now") LocalDateTime now);

    /** 특정 사용자의 기존 토큰 일괄 삭제(재발급 시 정리) */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PasswordResetToken t where t.userId = :userId")
    void  deleteByUserId(@Param("userId") Long userId);

    /** 만료된 토큰 청소 작업 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PasswordResetToken t where t.expiresAt <= :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}