package com.koundary.domain.user.repository;

import com.koundary.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    // Optional<User> findByNickname(String nickname);
    // Optional<User> findByUniversityEmail(String universityEmail);
}
