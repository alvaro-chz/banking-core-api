package com.example.bank_app.dto.admin;

public record UserAdminResponse(
        Long id,
        String fullName,
        String documentId,
        String email,
        Boolean isBlocked
) {
}
