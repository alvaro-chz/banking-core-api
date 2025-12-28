package com.example.bank_app.controller;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.dto.transaction.DepositRequest;
import com.example.bank_app.dto.transaction.PayServiceRequest;
import com.example.bank_app.dto.transaction.TransferRequest;
import com.example.bank_app.dto.transaction.WithdrawRequest;
import com.example.bank_app.model.*;
import com.example.bank_app.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(username = "juan@test.com", roles = {"CLIENT"})
class TransactionControllerIT extends AbstractIntegrationTest {

    @Autowired private BankTransactionRepository bankTransactionRepository;
    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private TransactionStatusRepository transactionStatusRepository;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private User sourceUser;
    private BankAccount sourceAcc;
    private BankAccount targetAcc;
    private Currency currencyUSD;
    private Currency currencyPEN;
    private TransactionStatus transactionStatus;
    private TransactionType transferType;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.save(Role.builder()
                .id(1)
                .name("CLIENT")
                .build());

        AccountType type = accountTypeRepository.save(AccountType.builder()
                .id(1)
                .name("AHORROS")
                .build());

        currencyUSD = currencyRepository.save(Currency.builder()
                .id(1)
                .code("USD")
                .name("Dólares")
                .build());

        currencyPEN = currencyRepository.save(Currency.builder()
                .id(2)
                .code("PEN")
                .name("Soles")
                .build());

        sourceUser = userRepository.save(User.builder()
                .role(role)
                .name("Juan")
                .lastName1("Perez")
                .documentId("12345678")
                .email("juan@test.com")
                .password("pass")
                .phoneNumber("999999")
                .build());

        User targetUser = userRepository.save(User.builder()
                .role(role)
                .name("Pedro")
                .lastName1("Gomez")
                .documentId("01234567")
                .email("pedro@test.com")
                .password("pass")
                .phoneNumber("888888")
                .build());

        sourceAcc = bankAccountRepository.save(BankAccount.builder()
                .user(sourceUser)
                .accountType(type)
                .accountNumber("1234567")
                .currency(currencyUSD)
                .currentBalance(BigDecimal.valueOf(500))
                .build());

        targetAcc = bankAccountRepository.save(BankAccount.builder()
                .user(targetUser)
                .accountType(type)
                .accountNumber("0001112")
                .currency(currencyUSD)
                .currentBalance(BigDecimal.valueOf(700))
                .build());

        transactionStatus = transactionStatusRepository.save(TransactionStatus.builder()
                .id(2)
                .name("SUCCESS")
                .build());

        transferType = transactionTypeRepository.save(TransactionType.builder().id(1).name("TRANSFERENCIA").build());
        transactionTypeRepository.save(TransactionType.builder().id(2).name("DEPOSITO").build());
        transactionTypeRepository.save(TransactionType.builder().id(3).name("RETIRO").build());
        transactionTypeRepository.save(TransactionType.builder().id(4).name("PAGO_SERVICIO").build());
    }

    // --- POST Tests ---

    @Test
    void transfer_ShouldCreateTransferTransaction() throws Exception {
        // Given
        TransferRequest request = new TransferRequest(
                "1234567",
                "0001112",
                BigDecimal.valueOf(100),
                "USD",
                "transfer"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/transactions/user/{id}/transfer", sourceUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceAccount").value("1234567"))
                .andExpect(jsonPath("$.targetAccount").value("0001112"))
                .andExpect(jsonPath("$.transactionType").value("TRANSFERENCIA"));
    }

    @Test
    void deposit_ShouldCreateDepositTransaction() throws Exception {
        // Given
        DepositRequest request = new DepositRequest(
                "1234567",
                BigDecimal.valueOf(500),
                "PEN",
                "deposit"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceAccount").value("EXTERNO"))
                .andExpect(jsonPath("$.targetAccount").value("1234567"))
                .andExpect(jsonPath("$.transactionType").value("DEPOSITO"));
    }

    @Test
    void withdraw_ShouldCreateWithdrawTransaction() throws Exception {
        // Given
        WithdrawRequest request = new WithdrawRequest(
                "1234567",
                BigDecimal.valueOf(200),
                "PEN",
                "withdraw"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/transactions/user/{id}/withdraw", sourceUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceAccount").value("1234567"))
                .andExpect(jsonPath("$.targetAccount").value("EXTERNO/VENTANILLA"))
                .andExpect(jsonPath("$.transactionType").value("RETIRO"));
    }

    @Test
    void payment_ShouldCreatePaymentTransaction() throws Exception {
        // Given
        PayServiceRequest request = new PayServiceRequest(
                "1234567",
                BigDecimal.valueOf(300),
                "USD",
                "pay service",
                "test service",
                "0-123-002"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/transactions/user/{id}/payment", sourceUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceAccount").value("1234567"))
                .andExpect(jsonPath("$.targetAccount").value("EXTERNO/VENTANILLA"))
                .andExpect(jsonPath("$.transactionType").value("PAGO_SERVICIO"));
    }

    // --- GET Tests ---

    @Test
    void get_ShouldReturnListOfAllTransactions() throws Exception {
        // Given
        bankTransactionRepository.saveAndFlush(BankTransaction.builder()
                .sourceAccount(sourceAcc)
                .targetAccount(targetAcc)
                .transactionType(transferType)
                .amount(BigDecimal.valueOf(300))
                .currency(currencyUSD)
                .transactionStatus(transactionStatus)
                .referenceCode("REF-1")
                .description("transfer")
                .build());

        bankTransactionRepository.saveAndFlush(BankTransaction.builder()
                .sourceAccount(sourceAcc)
                .targetAccount(targetAcc)
                .transactionType(transferType)
                .amount(BigDecimal.valueOf(500))
                .currency(currencyPEN)
                .transactionStatus(transactionStatus)
                .referenceCode("REF-2")
                .description("transfer 2")
                .build());

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/history/account/{accountNumber}?userId={userId}", sourceAcc.getAccountNumber(),sourceUser.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].referenceCode").value("REF-2"))
                .andExpect(jsonPath("$.content[1].referenceCode").value("REF-1"));
    }
}