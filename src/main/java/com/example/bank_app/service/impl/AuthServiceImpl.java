package com.example.bank_app.service.impl;

import com.example.bank_app.config.JwtService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName(),
                jwtToken
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

        Role defaultRole = roleRepository.findByName("CLIENT")
                .orElseThrow(() -> new RuntimeException("Error: Rol por defecto no encontrado en BD"));

        User user = User.builder()
                .name(request.name())
                .lastName1(request.lastName1())
                .lastName2(request.lastName2())
                .documentId(request.documentId())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .role(defaultRole)
                .build();

        User saved = userRepository.save(user);

        accountService.createAccount(new AccountCreationRequest(
                "PEN",
                "CORRIENTE"
        ), saved);

        String jwtToken = jwtService.generateToken(saved);

        return new AuthResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().getName(),
                jwtToken
        );
    }
}