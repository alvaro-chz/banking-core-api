package com.example.bank_app.controller;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.dto.auth.LoginRequest;
import com.example.bank_app.dto.auth.RegisterRequest;
import com.example.bank_app.model.AccountType;
import com.example.bank_app.model.Currency;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.AccountTypeRepository;
import com.example.bank_app.repository.CurrencyRepository;
import com.example.bank_app.repository.RoleRepository;
import com.example.bank_app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;

    private Role clientRole;

    @BeforeEach
    void setUp() {
        // Limpiamos la BD para evitar conflictos de Unique Key (email/documentId)
        userRepository.deleteAll();
        roleRepository.deleteAll();

        clientRole = roleRepository.save(Role.builder()
                .id(1)
                .name("CLIENT")
                .build());

        currencyRepository.save(Currency.builder()
                .id(1)
                .code("PEN")
                .name("Soles")
                .build());

        accountTypeRepository.save(AccountType.builder()
                .id(1)
                .name("CORRIENTE")
                .build());

        userRepository.save(User.builder()
                .role(clientRole)
                .name("Existing")
                .lastName1("User")
                .documentId("88888888")
                .email("login@test.com")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("999999999")
                .isActive(true)
                .build());
    }

    // --- REGISTER Tests ---

    @Test
    void register_ShouldCreateUser_WhenRequestIsValid() throws Exception {
        // Given:
        RegisterRequest request = new RegisterRequest(
                "Nuevo",
                "Usuario",
                "Apellido2",
                "12345678",
                "nuevo@mail.com",
                "secretPass",
                "987654321"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("nuevo@mail.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void register_ShouldFail_WhenEmailIsInvalid() throws Exception {
        // Given: Email inválido
        RegisterRequest request = new RegisterRequest(
                "Name", "Last", null, "12345678",
                "not-an-email",
                "pass123", "999999"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // --- LOGIN Tests ---

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() throws Exception {
        // Given: El usuario creado en setUp ("login@test.com" / "password123")
        LoginRequest request = new LoginRequest(
                "login@test.com",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("login@test.com"));
    }

    @Test
    void login_ShouldFail_WhenPasswordIsWrong() throws Exception {
        // Given:
        LoginRequest request = new LoginRequest(
                "login@test.com",
                "WRONG_PASSWORD"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}