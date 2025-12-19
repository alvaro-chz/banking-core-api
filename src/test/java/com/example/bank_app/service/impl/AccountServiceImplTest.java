package com.example.bank_app.service.impl;

import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.dto.account.AccountResponse;
import com.example.bank_app.model.AccountType;
import com.example.bank_app.model.BankAccount;
import com.example.bank_app.model.Currency;
import com.example.bank_app.model.User;
import com.example.bank_app.repository.AccountTypeRepository;
import com.example.bank_app.repository.BankAccountRepository;
import com.example.bank_app.repository.CurrencyRepository;
import com.example.bank_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {
    @Mock private AccountTypeRepository accountTypeRepository;
    @Mock private BankAccountRepository bankAccountRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    // Variables comunes
    private User user;
    private Currency currencyPEN;
    private AccountType accountTypeSavings;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        currencyPEN = Currency.builder().id(1).code("PEN").name("Soles").build();
        accountTypeSavings = AccountType.builder().id(1).name("AHORROS").build();
    }

    // --- GetAccountsByUserId test ---

    @Test
    void getAccountsByUserIdSuccess() {
        // Given
        Long userId = 1L;
        BankAccount account = BankAccount.builder()
                .id(100L)
                .user(user)
                .currency(currencyPEN)
                .accountType(accountTypeSavings)
                .currentBalance(BigDecimal.valueOf(500.00))
                .accountNumber("12345678901234")
                .isActive(true)
                .build();

        when(bankAccountRepository.findAllByUserIdAndIsActiveTrue(userId))
                .thenReturn(List.of(account));

        // When
        List<AccountResponse> result = accountService.getAccountsByUserId(userId);

        // Then
        AccountResponse response = result.get(0);
        assertThat(result).hasSize(1);
        assertThat(response.accountNumber()).isEqualTo("12345678901234");
        assertThat(response.currency()).isEqualTo("Soles");
        assertThat(response.accountType()).isEqualTo("AHORROS");
    }

    // --- CreateAccount test---

    @Test
    void createAccountWithUserIdSuccess() {
        // Given
        Long userId = 1L;
        AccountCreationRequest request = new AccountCreationRequest(
                "PEN",
                "AHORROS"
        );

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(accountTypeRepository.findByName("AHORROS"))
                .thenReturn(Optional.of(accountTypeSavings));
        when(bankAccountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);
        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(inv -> {
                    BankAccount b = inv.getArgument(0);
                    b.setId(200L);
                    b.setCurrentBalance(BigDecimal.ZERO);
                    return b;
                });

        // When
        AccountResponse response = accountService.createAccount(request, userId);

        // Then
        assertThat(response.id()).isEqualTo(200L);
        assertThat(response.currency()).isEqualTo("Soles");
        verify(userRepository).findById(userId);
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void createAccount_CollisionRetry() {
        // Given
        AccountCreationRequest request = new AccountCreationRequest(
                "PEN",
                "AHORROS");

        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(accountTypeRepository.findByName("AHORROS"))
                .thenReturn(Optional.of(accountTypeSavings));

        when(bankAccountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        when(bankAccountRepository.save(any(BankAccount.class)))
                .thenAnswer(inv -> {
                    BankAccount b = inv.getArgument(0);
                    b.setId(200L);
                    b.setCurrentBalance(BigDecimal.ZERO);
                    return b;
                });

        // When
        AccountResponse response = accountService.createAccount(request, user);

        // Then
        assertThat(response).isNotNull();
        verify(bankAccountRepository, times(2)).existsByAccountNumber(anyString());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }
}