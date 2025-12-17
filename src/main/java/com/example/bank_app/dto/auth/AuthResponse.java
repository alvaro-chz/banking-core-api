package com.example.bank_app.dto.auth;

public record AuthResponse(
        Long id,
        String name,
        String email,
        String role,
        String token
) {}
