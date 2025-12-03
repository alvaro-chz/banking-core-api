package com.example.bank_app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest (
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre como máximo debe tener 100 caracteres")
    String name,

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 100, message = "El primer apellido como máximo debe tener 100 caracteres")
    String lastName1,

    @Size(max = 100, message = "El segundo apellido como máximo debe tener 100 caracteres")
    String lastName2,

    @NotBlank(message = "El documento de identidad es obligatorio")
    @Size(min = 8, max = 12, message = "El DNI debe tener entre 8 y 12 caracteres")
    String documentId,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no cumple el formato")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña como mínimo debe tener 6 caracteres")
    String password,

    @NotBlank(message = "El telefono es requerido")
    @Size(min = 6, max = 20, message = "El telefono debe tener entre 6 a 20 caracteres")
    String phoneNumber
){
}
