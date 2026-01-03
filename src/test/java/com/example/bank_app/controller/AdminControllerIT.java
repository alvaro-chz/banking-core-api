package com.example.bank_app.controller;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.*;
import com.example.bank_app.model.enums.AuditAction;
import com.example.bank_app.repository.*;
import com.example.bank_app.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminControllerIT extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private BankTransactionRepository bankTransactionRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private TransactionStatusRepository transactionStatusRepository;
    @Autowired private LoginAttemptRepository loginAttemptRepository;

    @MockitoBean
    private AuditLogService auditLogService;

    private User adminUser;
    private User clientUser;
    private BankAccount clientAccount;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        loginAttemptRepository.deleteAll();
        bankTransactionRepository.deleteAll();
        bankAccountRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Data
        Role adminRole = roleRepository.save(Role.builder().id(1).name("ADMIN").build());
        Role clientRole = roleRepository.save(Role.builder().id(2).name("CLIENT").build());
        Currency currencyUSD = currencyRepository.save(Currency.builder().id(1).code("USD").name("Dólares").build());
        AccountType accType = accountTypeRepository.save(AccountType.builder().id(1).name("AHORROS").build());
        TransactionStatus statusSuccess = transactionStatusRepository.save(TransactionStatus.builder().id(2).name("SUCCESS").build());
        TransactionType typeTransfer = transactionTypeRepository.save(TransactionType.builder().id(1).name("TRANSFERENCIA").build());

        // 2. Crear ADMIN
        adminUser = userRepository.save(User.builder()
                .role(adminRole)
                .name("Admin").lastName1("System").documentId("99999999")
                .email("admin@bank.com").password("pass").phoneNumber("000000000")
                .build());

        // Auth Manual
        auth = new UsernamePasswordAuthenticationToken(
                adminUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // 3. Crear CLIENTE
        clientUser = userRepository.save(User.builder()
                .role(clientRole)
                .name("Cliente").lastName1("Test").documentId("88888888")
                .email("client@bank.com").password("pass").phoneNumber("111111111")
                .build());

        // 4. Cuenta y Transacción
        clientAccount = bankAccountRepository.save(BankAccount.builder()
                .user(clientUser)
                .accountType(accType)
                .accountNumber("111222333")
                .currency(currencyUSD)
                .currentBalance(BigDecimal.valueOf(1000))
                .build());

        bankTransactionRepository.save(BankTransaction.builder()
                .sourceAccount(clientAccount)
                .transactionType(typeTransfer)
                .amount(BigDecimal.valueOf(100))
                .currency(currencyUSD)
                .transactionStatus(statusSuccess)
                .referenceCode("REF-ADM-01")
                .description("Transacción reciente")
                .build());
    }

    // --- Tests Dashboard ---

    @Test
    void getDashboard_ShouldReturnCorrectStructure() throws Exception {
        // Given:

        // When
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retainedUsersCount").exists());
    }

    // --- Tests Transactions ---

    @Test
    void getAllTransactions_FilterByAccountId_ShouldReturnTransactionsAndLogAction() throws Exception {
        // Given
        Long accountId = clientAccount.getId();

        // When
        mockMvc.perform(get("/api/v1/admin/transactions")
                        .with(authentication(auth))
                        .param("accountId", String.valueOf(accountId))
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(auditLogService).logAction(
                eq(adminUser.getId()),
                eq(AuditAction.SEARCH_TRANSACTIONS),
                contains("ID: " + accountId),
                any(),
                any()
        );
    }

    @Test
    void getAllTransactions_FilterByDateAndStatus_ShouldReturnFiltered() throws Exception {
        // Given
        TransactionStatus statusFailed = transactionStatusRepository.save(TransactionStatus.builder().id(3).name("FAILED").build());
        BankTransaction oldTx = BankTransaction.builder()
                .sourceAccount(clientAccount)
                .transactionType(transactionTypeRepository.findById(1).orElseThrow())
                .amount(BigDecimal.valueOf(50))
                .currency(clientAccount.getCurrency())
                .transactionStatus(statusFailed)
                .referenceCode("REF-OLD-01")
                .description("Transacción antigua")
                .build();
        bankTransactionRepository.save(oldTx);

        String today = LocalDate.now().toString();

        // When
        mockMvc.perform(get("/api/v1/admin/transactions")
                        .with(authentication(auth))
                        .param("status", "SUCCESS")
                        .param("fromDate", today)
                        .param("toDate", today)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // --- Tests Users ---

    @Test
    void getUsers_WithTerm_ShouldReturnFilteredAndLogAction() throws Exception {
        // Given
        String term = "client@bank.com";

        // When
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(authentication(auth))
                        .param("term", term)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value(term));

        verify(auditLogService).logAction(
                eq(adminUser.getId()),
                eq(AuditAction.SEARCH_USERS),
                contains(term),
                any(),
                any()
        );
    }

    // --- Tests Unblock ---

    @Test
    void unblockUser_ShouldUnblockAndLogAction() throws Exception {
        // Given:
        LoginAttempt blockedAttempt = LoginAttempt.builder()
                .user(clientUser)
                .attempts(5)
                .isBlocked(true)
                .lastAttempt(LocalDateTime.now())
                .build();
        loginAttemptRepository.save(blockedAttempt);

        // When
        mockMvc.perform(patch("/api/v1/admin/users/{id}/unblock", clientUser.getId())
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Then:
        LoginAttempt updatedAttempt = loginAttemptRepository.findByUserId(clientUser.getId()).orElseThrow();
        assertFalse(updatedAttempt.getIsBlocked());

        verify(auditLogService).logAction(
                eq(adminUser.getId()),
                eq(AuditAction.UNBLOCK_USER),
                contains("ID: " + clientUser.getId()),
                any(),
                any()
        );
    }
}