package com.example.bank_app.controller;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.dto.beneficiary.BeneficiaryCreateRequest;
import com.example.bank_app.dto.beneficiary.BeneficiaryUpdateRequest;
import com.example.bank_app.model.Beneficiary;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.BeneficiaryRepository;
import com.example.bank_app.repository.RoleRepository;
import com.example.bank_app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser(username = "juan@test.com", roles = {"CLIENT"})
class BeneficiaryControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

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
                .password("pass")
                .phoneNumber("999999")
                .isActive(true)
                .build());
    }

    // --- GET Tests ---

    @Test
    void getAll_ShouldReturnList_WhenBeneficiariesExist() throws Exception {
        beneficiaryRepository.save(Beneficiary.builder()
                .user(defaultUser)
                .accountNumber("BCP-111")
                .bankName("BCP")
                .alias("Mi cuenta BCP")
                .build());

        // When & Then:
        mockMvc.perform(get("/api/v1/beneficiaries/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].alias").value("Mi cuenta BCP"))
                .andExpect(jsonPath("$[0].accountNumber").value("BCP-111"));
    }

    // --- POST Tests ---

    @Test
    void add_ShouldCreateBeneficiary_WhenRequestIsValid() throws Exception {
        // Given:
        BeneficiaryCreateRequest request = new BeneficiaryCreateRequest(
                "BBVA-222",
                "BBVA",
                "Pago Alquiler"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.alias").value("Pago Alquiler"));
    }

    @Test
    void add_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given: DTO inválido (accountNumber vacío)
        BeneficiaryCreateRequest invalidRequest = new BeneficiaryCreateRequest(
                "",
                "Bank",
                "Alias"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/beneficiaries/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- PUT Tests ---

    @Test
    void update_ShouldModifyBeneficiary() throws Exception {
        // Given:
        Beneficiary saved = beneficiaryRepository.save(Beneficiary.builder()
                .user(defaultUser)
                .accountNumber("OLD-NUM")
                .bankName("OLD-BANK")
                .alias("Old Alias")
                .build());

        BeneficiaryUpdateRequest updateRequest = new BeneficiaryUpdateRequest(
                "NEW-NUM",
                "NEW-BANK",
                "New Alias"
        );

        // When & Then
        mockMvc.perform(put("/api/v1/beneficiaries/user/{userId}/{id}", defaultUser.getId(), saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("New Alias"))
                .andExpect(jsonPath("$.accountNumber").value("NEW-NUM"));
    }

    // --- DELETE Tests ---

    @Test
    void delete_ShouldRemoveBeneficiary() throws Exception {
        // Given
        Beneficiary saved = beneficiaryRepository.save(Beneficiary.builder()
                .user(defaultUser)
                .accountNumber("DEL-123")
                .bankName("X")
                .alias("To Delete")
                .build());

        // When
        mockMvc.perform(delete("/api/v1/beneficiaries/user/{userId}/{id}", defaultUser.getId(), saved.getId()))
                .andExpect(status().isOk());

        // Then:
        mockMvc.perform(get("/api/v1/beneficiaries/user/{id}", defaultUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}