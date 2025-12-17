package com.example.bank_app.dto.user;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String name,
    String lastName1,
    String lastName2,
    String documentId,
    String email,
    String phoneNumber,
    String role,
    LocalDateTime createdAt
){ }
