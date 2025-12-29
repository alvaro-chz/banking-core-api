package com.example.bank_app.repository;

import com.example.bank_app.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    long countByIsBlockedTrue();
    Optional<LoginAttempt> findByUserIdAndIsBlockedTrue(Long userId);
    @Query("SELECT la FROM LoginAttempt la JOIN FETCH la.user WHERE la.isBlocked = true ORDER BY la.lastAttempt DESC")
    List<LoginAttempt> findLastBlockedUsers(Pageable pageable);
    Optional<LoginAttempt> findByUserId(Long userId);
}
