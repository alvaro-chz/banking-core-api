package com.example.bank_app.service.impl;

import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.dto.auth.AuthResponse;
import com.example.bank_app.dto.auth.LoginRequest;
import com.example.bank_app.dto.auth.RegisterRequest;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.RoleRepository;
import com.example.bank_app.repository.UserRepository;
import com.example.bank_app.service.AccountService;
import com.example.bank_app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: Encriptar password (JWT)

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountService accountService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(request.password())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                String.valueOf(user.getPassword().hashCode())
        );
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (userRepository.existsByDocumentId(request.documentId())) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        Role defaultRole = roleRepository.findById(2) // Cliente
                .orElseThrow(() -> new RuntimeException("Error: Rol por defecto no encontrado en BD"));

        User user = User.builder()
                .name(request.name())
                .lastName1(request.lastName1())
                .lastName2(request.lastName2())
                .documentId(request.documentId())
                .email(request.email())
                .password(request.password())
                .phoneNumber(request.phoneNumber())
                .role(defaultRole)
                .build();

        User saved = userRepository.save(user);

        accountService.createAccount(new AccountCreationRequest(
                "PEN",
                "CORRIENTE"
        ), saved);

        return new AuthResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().getName(),
                String.valueOf(saved.getPassword().hashCode())
        );
    }
}