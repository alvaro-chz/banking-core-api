package com.example.bank_app.repository;

import com.example.bank_app.AbstractIntegrationTest;
import com.example.bank_app.model.AccountType;
import com.example.bank_app.model.BankAccount;
import com.example.bank_app.model.Currency;
import com.example.bank_app.model.Role;
import com.example.bank_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankAccountRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private User defaultUser;
    private AccountType savingsType;
    private Currency usdCurrency;

    @BeforeEach
    void setUp() {
        Role clientRole = roleRepository.save(Role.builder()
                .id(2)
                .name("CLIENT")
                .build());

        defaultUser = userRepository.save(User.builder()
                .role(clientRole)
                .name("Account Owner")
                .lastName1("Tester")
                .documentId("11223344")
                .email("owner@bank.com")
                .password("pass")
                .phoneNumber("555-0000")
                .isActive(true)
                .build());

        savingsType = accountTypeRepository.save(AccountType.builder()
                .id(1)
                .name("AHORROS")
                .build());

        usdCurrency = currencyRepository.save(Currency.builder()
                .id(1)
                .name("US Dollar")
                .code("USD")
                .build());
    }

    // --- Basic CRUD & Auditing ---

    @Test
    void save_Success_And_CheckDefaults() {
        // Given
        BankAccount account = BankAccount.builder()
                .user(defaultUser)
                .accountType(savingsType)
                .currency(usdCurrency)
                .accountNumber("100-200-300")
                .build();

        // When
        BankAccount saved = bankAccountRepository.save(account);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getIsActive()).isTrue();
        assertThat(saved.getUser().getId()).isEqualTo(defaultUser.getId());
    }

    // --- Query Methods Tests ---

    @Test
    void findByAccountNumberAndIsActiveTrue_Behaviors() {
        // Given: Una cuenta activa y una inactiva
        createAccount("ACT-001", true);
        createAccount("INA-001", false);

        // When
        Optional<BankAccount> activeFound = bankAccountRepository.findByAccountNumberAndIsActiveTrue("ACT-001");
        Optional<BankAccount> inactiveFound = bankAccountRepository.findByAccountNumberAndIsActiveTrue("INA-001");

        // Then
        assertThat(activeFound).isPresent();
        assertThat(inactiveFound).isEmpty();
    }

    @Test
    void findAllByUserIdAndIsActiveTrue_ShouldReturnOnlyActive() {
        // Given
        createAccount("ACC-1", true);
        createAccount("ACC-2", true);
        createAccount("ACC-3", false);

        // When
        List<BankAccount> accounts = bankAccountRepository.findAllByUserIdAndIsActiveTrue(defaultUser.getId());

        // Then
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(BankAccount::getAccountNumber)
                .containsExactlyInAnyOrder("ACC-1", "ACC-2");
    }

    @Test
    void existsByAccountNumber_Checks() {
        // Given
        createAccount("EXISTS-1", true);

        // When & Then
        assertThat(bankAccountRepository.existsByAccountNumber("EXISTS-1")).isTrue();
        assertThat(bankAccountRepository.existsByAccountNumber("GHOST-1")).isFalse();
    }

    @Test
    void existsByAccountNumberAndUserId_Checks() {
        // Given
        createAccount("USER-ACC-1", true);

        Role role = roleRepository.findAll().get(0);
        User otherUser = userRepository.save(User.builder()
                .role(role)
                .name("Other")
                .lastName1("P")
                .documentId("999")
                .email("o@m.com")
                .password("p")
                .phoneNumber("1")
                .build());

        // When & Then
        assertThat(bankAccountRepository.existsByAccountNumberAndUserId("USER-ACC-1", defaultUser.getId())).isTrue();
        assertThat(bankAccountRepository.existsByAccountNumberAndUserId("USER-ACC-1", otherUser.getId())).isFalse();
    }

    // --- DB Constraints Tests ---

    @Test
    void save_Fail_DuplicateAccountNumber() {
        // Given
        createAccount("DUPLICATE-1", true);

        BankAccount duplicate = BankAccount.builder()
                .user(defaultUser)
                .accountType(savingsType)
                .currency(usdCurrency)
                .accountNumber("DUPLICATE-1")
                .currentBalance(BigDecimal.TEN)
                .build();

        // When & Then
        assertThatThrownBy(() -> bankAccountRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- Helper Method ---
    private BankAccount createAccount(String accountNumber, boolean isActive) {
        return bankAccountRepository.save(BankAccount.builder()
                .user(defaultUser)
                .accountType(savingsType)
                .currency(usdCurrency)
                .accountNumber(accountNumber)
                .isActive(isActive)
                .build());
    }
}