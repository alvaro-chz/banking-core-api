package com.example.bank_app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El campo email no debe estar vacío")
    @Email(message = "El email no cumple el formato")
    String email,

    @NotBlank(message = "El campo contraseña no debe estar vacío")
    String password
) { }
