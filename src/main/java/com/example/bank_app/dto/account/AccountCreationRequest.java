package com.example.bank_app.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountCreationRequest (
    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "El código de moneda debe tener 3 letras (Ej: USD)")
    String currency,

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    String accountType
){
}
