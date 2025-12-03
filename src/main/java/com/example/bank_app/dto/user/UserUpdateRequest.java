package com.example.bank_app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Email(message = "El email no cumple el formato")
    String email,

    @Size(min = 6, max = 20, message = "El telefono debe tener entre 6 a 20 caracteres")
    String phoneNumber
) {
}
