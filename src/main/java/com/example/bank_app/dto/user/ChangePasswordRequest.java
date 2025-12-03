package com.example.bank_app.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "La contraseña actual es requerida")
    String currentPassword,

    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 6, message = "La nueva contraseña debe tener mínimo 6 caracteres")
    String newPassword,

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    String confirmationPassword
) {}