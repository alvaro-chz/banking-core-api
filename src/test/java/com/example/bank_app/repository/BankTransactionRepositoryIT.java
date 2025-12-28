package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankTransactionRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private BankTransactionRepository bankTransactionRepository;

    @Autowired private BankAccountRepository bankAccountRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private AccountTypeRepository accountTypeRepository;
    @Autowired private CurrencyRepository currencyRepository;
    @Autowired private TransactionTypeRepository transactionTypeRepository;
    @Autowired private TransactionStatusRepository transactionStatusRepository;

    private BankAccount accountA;
    private BankAccount accountB;
    private TransactionType typeTransfer;
    private TransactionStatus statusCompleted;
    private Currency currency;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.save(Role.builder()
                .id(1)
                .name("CLIENT")
                .build());

        User user = userRepository.save(User.builder()
                .role(role)
                .name("User")
                .lastName1("T")
                .documentId("111")
                .email("u@t.com")
                .password("p")
                .phoneNumber("1")
                .build());

        AccountType accType = accountTypeRepository.save(AccountType.builder()
                .id(1)
                .name("AHORROS")
                .build());

        currency = currencyRepository.save(Currency.builder()
                .id(1)
                .name("USD")
                .code("USD")
                .build());

        typeTransfer = transactionTypeRepository.save(TransactionType.builder()
                .id(1)
                .name("TRANSFERENCIA")
                .build());

        statusCompleted = transactionStatusRepository.save(TransactionStatus.builder()
                .id(1)
                .name("SUCCESS")
                .build());

        accountA = bankAccountRepository.save(BankAccount.builder()
                .user(user)
                .accountType(accType)
                .currency(currency)
                .accountNumber("ACC-A")
                .currentBalance(new BigDecimal("1000"))
                .build());

        accountB = bankAccountRepository.save(BankAccount.builder()
                .user(user)
                .accountType(accType)
                .currency(currency)
                .accountNumber("ACC-B")
                .currentBalance(new BigDecimal("500"))
                .build());
    }

    // --- Basic CRUD & Auditing ---

    @Test
    void save_Success_And_Audit() {
        // Given
        BankTransaction transaction = BankTransaction.builder()
                .sourceAccount(accountA)
                .targetAccount(accountB)
                .amount(new BigDecimal("100.00"))
                .currency(currency)
                .transactionType(typeTransfer)
                .transactionStatus(statusCompleted)
                .referenceCode(UUID.randomUUID().toString())
                .description("Payment for services")
                .build();

        // When
        BankTransaction saved = bankTransactionRepository.save(transaction);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("100.00");
        assertThat(saved.getSourceAccount().getAccountNumber()).isEqualTo("ACC-A");
    }

    // --- Custom Query Test (@Query) ---

    @Test
    void findAllByAccountId_ShouldReturnIncomingAndOutgoing() {
        // Scenario:
        // 1. A envía dinero a B (A es source)
        createTransaction(accountA, accountB, "REF-1");

        // 2. B envía dinero a A (A es target)
        createTransaction(accountB, accountA, "REF-2");

        // 3. B envía dinero a una cuenta externa (A no participa)
        createTransaction(accountB, null, "REF-3");

        Pageable pageable = PageRequest.of(0, 10);

        // When: Buscamos todas las transacciones de A
        Page<BankTransaction> transactionsOfA = bankTransactionRepository.findAllByAccountId(
                accountA.getId(),
                null,
                null,
                null,
                pageable
        );

        // Then:
        assertThat(transactionsOfA.getTotalElements()).isEqualTo(2);
        assertThat(transactionsOfA.getContent())
                .extracting(BankTransaction::getReferenceCode)
                .containsExactlyInAnyOrder("REF-1", "REF-2");
    }

    // --- Query Method Tests ---

    @Test
    void existsByReferenceCode_Check() {
        // Given
        String code = "UNIQUE-CODE-123";
        createTransaction(accountA, accountB, code);

        // When & Then
        assertThat(bankTransactionRepository.existsByReferenceCode(code)).isTrue();
        assertThat(bankTransactionRepository.existsByReferenceCode("NON-EXISTENT")).isFalse();
    }

    // --- DB Constraints ---

    @Test
    void save_Fail_DuplicateReferenceCode() {
        // Given
        createTransaction(accountA, accountB, "DUPLICATE-REF");

        BankTransaction duplicate = BankTransaction.builder()
                .sourceAccount(accountA)
                .targetAccount(accountB)
                .amount(BigDecimal.TEN)
                .currency(currency)
                .transactionType(typeTransfer)
                .transactionStatus(statusCompleted)
                .referenceCode("DUPLICATE-REF")
                .build();

        // When & Then
        assertThatThrownBy(() -> bankTransactionRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- Helper Method ---
    private void createTransaction(BankAccount source, BankAccount target, String refCode) {
        bankTransactionRepository.save(BankTransaction.builder()
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("50"))
                .currency(currency)
                .transactionType(typeTransfer)
                .transactionStatus(statusCompleted)
                .referenceCode(refCode)
                .build());
    }
}