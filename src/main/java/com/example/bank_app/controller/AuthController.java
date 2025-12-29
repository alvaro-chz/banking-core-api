package com.example.bank_app.controller;

import com.example.bank_app.dto.auth.AuthResponse;
import com.example.bank_app.dto.auth.LoginRequest;
import com.example.bank_app.dto.auth.RegisterRequest;
import com.example.bank_app.model.AuditLog;
import com.example.bank_app.model.User;
import com.example.bank_app.model.enums.AuditAction;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.AuditLogService;
import com.example.bank_app.service.AuthService;
import com.example.bank_app.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuditLogService auditLogService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request);

        auditLogService.logAction(
                response.id(),
                AuditAction.LOGIN_SUCCESS,
                "Inicio de sesión exitoso",
                WebUtils.getClientIp(httpRequest),
                WebUtils.getUserAgent(httpRequest)
        );

        return ResponseEntity.ok(response);
    }
}
