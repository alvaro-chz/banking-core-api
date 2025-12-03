package com.example.bank_app.dto.contact;

public record BeneficiaryResponse(
        Integer id,
        String alias,
        String accountNumber,
        String bankName
) {
}
