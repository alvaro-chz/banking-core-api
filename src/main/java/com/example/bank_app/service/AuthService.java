package com.example.bank_app.service;

import com.example.bank_app.dto.auth.AuthResponse;
import com.example.bank_app.dto.auth.LoginRequest;
import com.example.bank_app.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
