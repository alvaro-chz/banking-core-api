package com.example.bank_app.exception;

import java.time.LocalDateTime;

// Un objeto simple para estandarizar la respuesta de error
public record ErrorResponse(
        LocalDateTime timestamp,  // Cuándo pasó
        int status,               // Código HTTP (400, 404, 500)
        String error,             // Nombre del error (Bad Request, etc.)
        String message,           // Mensaje detallado (ej: "Saldo insuficiente")
        String path               // En qué endpoint ocurrió
) {}