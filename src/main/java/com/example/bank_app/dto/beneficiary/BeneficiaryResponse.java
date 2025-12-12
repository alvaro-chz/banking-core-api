package com.example.bank_app.dto.beneficiary;

public record BeneficiaryResponse(
        Integer id,
        String alias,
        String accountNumber,
        String bankName
) {
}
