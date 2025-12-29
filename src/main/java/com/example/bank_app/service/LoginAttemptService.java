package com.example.bank_app.service;

import com.example.bank_app.model.User;

public interface LoginAttemptService {
    void unblockUser(Long userId);
    void loginSucceeded(User user);
    void loginFailed(User user);
    void createLoginAttempt(User user);
    boolean getBlockStatus(User user);
}

