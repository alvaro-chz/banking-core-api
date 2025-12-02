package com.example.bank_app.repository;

import com.example.bank_app.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Integer> {
}
