package com.example.bank_app.dto.admin;

import java.time.LocalDateTime;

public record UserAdminResponse(
        Long id,
        String fullName,
        String documentId,
        String email,
        String phoneNumber,
        Boolean isBlocked,
        LocalDateTime createdAt
) {
}
