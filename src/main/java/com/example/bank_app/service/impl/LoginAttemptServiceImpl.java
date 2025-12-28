package com.example.bank_app.service.impl;

import com.example.bank_app.model.LoginAttempt;
import com.example.bank_app.repository.LoginAttemptRepository;
import com.example.bank_app.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUserIdAndIsBlockedTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no bloqueado o sin registro de intentos"));

        loginAttempt.setAttempts(0);
        loginAttempt.setIsBlocked(false);
        loginAttempt.setLastAttempt(null);
    }
}
