package com.example.bank_app.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PayServiceRequest(
        @NotBlank(message = "La cuenta de origen es obligatoria")
        String sourceAccount,

        @NotNull(message = "El monto es obligatorio")
        @Positive
        BigDecimal amount,

        @NotBlank(message = "La moneda es obligatoria")
        @Size(min = 3, max = 3, message = "El código de moneda debe tener 3 letras (Ej: USD)")
        String currency,

        @Size(max = 255, message = "No se debe exceder los 255 caracteres")
        String description,

        @NotBlank(message = "El nombre del servicio es obligatorio")
        String serviceName,

        @NotBlank(message = "El código de suministro es obligatorio")
        String supplyCode
) {
}
