package com.koundary.domain.auth.repository;

import com.koundary.domain.auth.entity.PasswordHistory;
import com.koundary.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);
}
