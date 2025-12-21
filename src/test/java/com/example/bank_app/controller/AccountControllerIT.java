package com.example.bank_app.controller;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.model.*;
import com.example.bank_app.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(username = "juan@test.com", roles = {"CLIENT"})
class AccountControllerIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;

    private User defaultUser;
    private Currency currencyUSD;
    private AccountType typeSavings;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.save(Role.builder().id(1).name("CLIENT").build());
        currencyUSD = currencyRepository.save(Currency.builder().id(1).code("USD").name("Dólares").build());
        typeSavings = accountTypeRepository.save(AccountType.builder().id(1).name("AHORROS").build());

        defaultUser = userRepository.save(User.builder()
                .role(role)
                .name("Juan")
                .lastName1("Perez")
                .documentId("12345678")
                .email("juan@test.com")
                .password("pass")
                .phoneNumber("999999")
                .build());
    }

    // --- POST Tests ---

    @Test
    void create_ShouldCreateAccount_WhenRequestIsValid() throws Exception {
        // Given
        AccountCreationRequest request = new AccountCreationRequest(
                "USD",
                "AHORROS"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/accounts/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.currency").value("Dólares"))
                .andExpect(jsonPath("$.accountType").value("AHORROS"))
                .andExpect(jsonPath("$.currentBalance").value(0));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given: Request inválido (moneda vacía)
        AccountCreationRequest invalidRequest = new AccountCreationRequest(
                "",
                "AHORROS"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/accounts/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- GET Tests ---

    @Test
    void getAccounts_ShouldReturnListOfAccounts() throws Exception {
        // Given:
        bankAccountRepository.save(BankAccount.builder()
                .user(defaultUser)
                .accountType(typeSavings)
                .currency(currencyUSD)
                .accountNumber("111-222-333")
                .currentBalance(new BigDecimal("1500.00"))
                .isActive(true)
                .build());

        // When & Then
        mockMvc.perform(get("/api/v1/accounts/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountNumber").value("111-222-333"))
                .andExpect(jsonPath("$[0].currency").value("Dólares"))
                .andExpect(jsonPath("$[0].currentBalance").value(1500.00));
    }

    @Test
    void getAccounts_ShouldReturnEmptyList_WhenNoAccountsExist() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/accounts/user/{id}", defaultUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}