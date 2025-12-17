package com.example.bank_app.dto.beneficiary;

public record BeneficiaryResponse(
        Long id,
        String alias,
        String accountNumber,
        String bankName
) {
}
