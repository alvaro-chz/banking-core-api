package com.example.bank_app.controller;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.dto.user.ChangePasswordRequest;
import com.example.bank_app.dto.user.UserUpdateRequest;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.RoleRepository;
import com.example.bank_app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(username = "juan@test.com", roles = {"CLIENT"})
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.save(Role.builder()
                .id(1)
                .name("CLIENT")
                .build());

        defaultUser = userRepository.save(User.builder()
                .role(role)
                .name("Juan")
                .lastName1("Perez")
                .documentId("12345678")
                .email("juan@test.com")
                .password(passwordEncoder.encode("securePass"))
                .phoneNumber("999999")
                .isActive(true)
                .build());
    }

    // --- GET Tests ---

    @Test
    void getProfile_ShouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@test.com"))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.documentId").value("12345678"));
    }

    // --- PUT Tests ---

    @Test
    void update_ShouldUpdateEmailAndPhone_WhenRequestIsValid() throws Exception {
        // Given
        UserUpdateRequest request = new UserUpdateRequest(
                "newemail@test.com",
                "123456789"
        );

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@test.com"))
                .andExpect(jsonPath("$.phoneNumber").value("123456789"))
                .andExpect(jsonPath("$.name").value("Juan"));
    }

    @Test
    void update_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        // Given
        UserUpdateRequest invalidRequest = new UserUpdateRequest(
                "not-an-email",
                "123456"
        );

        // When & Then
        mockMvc.perform(put("/api/v1/users/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- PATCH Tests ---

    @Test
    void changePassword_ShouldSucceed_WhenCurrentPasswordIsCorrect() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "securePass",
                "newPass123",
                "newPass123"
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_ShouldFail_WhenNewPasswordIsTooShort() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(
                "securePass",
                "123",
                "123"
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{id}/password", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}