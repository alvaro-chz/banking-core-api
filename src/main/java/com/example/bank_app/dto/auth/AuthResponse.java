package com.example.bank_app.dto.auth;

public record AuthResponse(
        Integer id,
        String name,
        String email,
        String role,
        String token
) {}
