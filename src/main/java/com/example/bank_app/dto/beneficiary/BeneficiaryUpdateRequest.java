package com.example.bank_app.dto.beneficiary;

import jakarta.validation.constraints.Size;

public record BeneficiaryUpdateRequest(
    String accountNumber,

    @Size(max = 100, message = "El nombre del banco no debe exceder los 100 caracteres")
    String bankName,

    @Size(max = 100, message = "El alias no debe exceder los 100 caracteres")
    String alias
) {
}
