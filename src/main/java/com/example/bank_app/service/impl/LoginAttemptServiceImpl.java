package com.example.bank_app.service.impl;

import com.example.bank_app.model.LoginAttempt;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.LoginAttemptRepository;
import com.example.bank_app.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    @Override
    public void unblockUser(Long userId) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUserIdAndIsBlockedTrue(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no bloqueado o sin registro de intentos"));

        loginAttempt.setAttempts(0);
        loginAttempt.setIsBlocked(false);
        loginAttempt.setLastAttempt(null);
    }

    @Override
    public void loginSucceeded(User user) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Relación no creada"));

        loginAttempt.setAttempts(0);
        loginAttempt.setIsBlocked(false);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginFailed(User user) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Relación no creada"));

        if (loginAttempt.getAttempts() == 2) {
            loginAttempt.setIsBlocked(true);
        }

        if (loginAttempt.getAttempts() >= 4) {
            loginAttempt.setIsBlocked(true);
        }

        loginAttempt.setAttempts(loginAttempt.getAttempts() + 1);
    }



    @Override
    public void createLoginAttempt(User user) {
        loginAttemptRepository.save(LoginAttempt.builder()
                .user(user)
                .build());
    }

    @Override
    public boolean getBlockStatus(User user) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Relación no creada"));

        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        if (loginAttempt.getAttempts() < 5 && loginAttempt.getIsBlocked() && loginAttempt.getLastAttempt().isBefore(tenMinutesAgo)) {
            loginAttempt.setIsBlocked(false);
        }

        return loginAttempt.getIsBlocked();
    }
}
