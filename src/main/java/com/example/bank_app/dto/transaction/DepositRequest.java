package com.example.bank_app.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DepositRequest(
        @NotBlank(message = "La cuenta de destino es obligatoria")
        String targetAccount,

        @NotNull(message = "El monto es obligatorio")
        @Positive
        BigDecimal amount,

        @NotBlank(message = "La moneda es obligatoria")
        @Size(min = 3, max = 3, message = "El código de moneda debe tener 3 letras (Ej: USD)")
        String currency,

        @Size(max = 255, message = "No se debe exceder los 255 caracteres")
        String description
) {
}
