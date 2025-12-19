package com.example.bank_app.service.impl;

import com.example.bank_app.dto.transaction.*;
import com.example.bank_app.model.*;
import com.example.bank_app.repository.*;
import com.example.bank_app.service.CurrencyExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private BankAccountRepository bankAccountRepository;
    @Mock private CurrencyRepository currencyRepository;
    @Mock private CurrencyExchangeService currencyExchangeService;
    @Mock private TransactionTypeRepository transactionTypeRepository;
    @Mock private TransactionStatusRepository transactionStatusRepository;
    @Mock private BankTransactionRepository bankTransactionRepository;

    @InjectMocks private TransactionServiceImpl transactionService;

    private User user;
    private User targetUser;
    private Currency currencyUSD;
    private Currency currencyPEN;
    private BankAccount sourceAccount;
    private BankAccount targetAccount;
    private TransactionStatus statusSuccess;

    // Setup:
    @BeforeEach
    void setUp() {
        // 1. Datos básicos
        user = User.builder().id(1L).build();
        targetUser = User.builder().id(2L).build();

        currencyUSD = Currency.builder().code("USD").build();
        currencyPEN = Currency.builder().code("PEN").build();

        // 2. Cuentas
        sourceAccount = BankAccount.builder()
                .id(10L)
                .user(user)
                .accountNumber("111111")
                .currentBalance(new BigDecimal("500.00"))
                .currency(currencyPEN)
                .build();

        targetAccount = BankAccount.builder()
                .id(20L)
                .user(targetUser)
                .accountNumber("222222")
                .currentBalance(new BigDecimal("600.00"))
                .currency(currencyPEN)
                .build();

        // 3. Status
        statusSuccess = TransactionStatus.builder().name("SUCCESS").build();

        // lenient
        lenient().when(transactionStatusRepository.findById(2)).thenReturn(Optional.of(statusSuccess));
        lenient().when(bankTransactionRepository.save(any(BankTransaction.class)))
                .thenAnswer(inv -> {
                    BankTransaction t = inv.getArgument(0);
                    t.setId(123L);
                    t.setCreatedAt(LocalDateTime.now());
                    return t;
                });
    }

    // --- Transfer tests ---

    @Test
    void transferSuccess() {
        // Given
        TransferRequest request = new TransferRequest(
                "111111",
                "222222",
                BigDecimal.valueOf(10.00),
                "USD",
                "Test transfer"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("222222"))
                .thenReturn(Optional.of(targetAccount));
        when(currencyRepository.findByCode("USD"))
                .thenReturn(Optional.of(currencyUSD));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(37.00));

        TransactionType transactionType = TransactionType.builder().name("TRANSFERENCIA").build();
        when(transactionTypeRepository.findById(1)).thenReturn(Optional.of(transactionType));

        // When
        TransactionResponse response = transactionService.transfer(request, 1L);

        // Then
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(10.00));
        assertThat(response.transactionType()).isEqualTo("TRANSFERENCIA");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("463.00");
        assertThat(targetAccount.getCurrentBalance()).isEqualByComparingTo("637.00");
    }

    @Test
    void transfer_UserDoesNotOwnAccount() {
        // Given
        TransferRequest request = new TransferRequest(
                "111111",
                "222222",
                BigDecimal.valueOf(10.00),
                "USD",
                "Test transfer"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));

        // When & Then
        assertThatThrownBy(() -> transactionService.transfer(request, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La cuenta origen no te pertenece");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.00");
        assertThat(targetAccount.getCurrentBalance()).isEqualByComparingTo("600.00");
    }

    @Test
    void transfer_InsufficientBalance() {
        // Given
        TransferRequest request = new TransferRequest(
                "111111",
                "222222",
                BigDecimal.valueOf(10.00),
                "USD",
                "Test transfer"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("222222"))
                .thenReturn(Optional.of(targetAccount));
        when(currencyRepository.findByCode("USD"))
                .thenReturn(Optional.of(currencyUSD));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));

        // When & Then
        assertThatThrownBy(() -> transactionService.transfer(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Saldo no disponible");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.00");
        assertThat(targetAccount.getCurrentBalance()).isEqualByComparingTo("600.00");
    }

    // --- Deposit test ---

    @Test
    void depositSuccess() {
        // Given
        DepositRequest request = new DepositRequest(
                "222222",
                BigDecimal.valueOf(100.00),
                "USD",
                "Depósito Ventanilla"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("222222"))
                .thenReturn(Optional.of(targetAccount));
        when(currencyRepository.findByCode("USD"))
                .thenReturn(Optional.of(currencyUSD));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(370.00));

        TransactionType typeDeposit = TransactionType.builder().name("DEPOSITO").build();
        when(transactionTypeRepository.findById(2)).thenReturn(Optional.of(typeDeposit));

        // When
        TransactionResponse response = transactionService.deposit(request);

        // Then
        assertThat(response.targetAccount()).isEqualTo("222222");
        assertThat(targetAccount.getCurrentBalance()).isEqualByComparingTo("970.00");
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    // --- Withdraw tests ---

    @Test
    void withdrawSuccess() {
        // Given
        WithdrawRequest request = new WithdrawRequest(
                "111111",
                BigDecimal.valueOf(50.00),
                "PEN",
                "Retiro de prueba"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(50.00));

        TransactionType transactionType = TransactionType.builder().name("RETIRO").build();
        when(transactionTypeRepository.findById(3)).thenReturn(Optional.of(transactionType));

        // When
        TransactionResponse response = transactionService.withdraw(request, 1L);

        // Then
        assertThat(response.sourceAccount()).isEqualTo("111111");
        assertThat(response.transactionType()).isEqualTo("RETIRO");
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(50.00));
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("450.00");
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    @Test
    void withdraw_UserDoesNotOwnAccount() {
        // Given
        WithdrawRequest request = new WithdrawRequest(
                "111111",
                BigDecimal.valueOf(50.00),
                "PEN",
                "Retiro ilegitimo"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));

        // When & Then
        assertThatThrownBy(() -> transactionService.withdraw(request, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La cuenta origen no te pertenece");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void withdraw_InsufficientBalance() {
        // Given
        WithdrawRequest request = new WithdrawRequest(
                "111111",
                BigDecimal.valueOf(1000.00),
                "PEN",
                "Retiro gigante"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));

        // When & Then
        assertThatThrownBy(() -> transactionService.withdraw(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Saldo no disponible");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.00");
    }

    // --- PayService tests ---

    @Test
    void payServiceSuccess() {
        // Given
        PayServiceRequest request = new PayServiceRequest(
                "111111",
                BigDecimal.valueOf(100.00),
                "PEN",
                "Pago Luz Noviembre",
                "Enel",
                "123-456-789"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(100.00));

        TransactionType transactionType = TransactionType.builder().name("PAGO_SERVICIO").build();
        when(transactionTypeRepository.findById(4)).thenReturn(Optional.of(transactionType));

        // When
        TransactionResponse response = transactionService.payService(request, 1L);

        // Then
        assertThat(response.sourceAccount()).isEqualTo("111111");
        assertThat(response.transactionType()).isEqualTo("PAGO_SERVICIO");
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(100.00));
        assertThat(response.description())
                .contains("SERVICIO: Enel")
                .contains("SUMINISTRO: 123-456-789")
                .contains("Pago Luz Noviembre");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("400.00");
    }

    @Test
    void payService_UserDoesNotOwnAccount() {
        // Given
        PayServiceRequest request = new PayServiceRequest(
                "111111",
                BigDecimal.valueOf(100.00),
                "PEN",
                "Pago ajeno",
                "Enel",
                "123"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));

        // When & Then
        assertThatThrownBy(() -> transactionService.payService(request, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La cuenta origen no te pertenece");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    void payService_InsufficientBalance() {
        // Given
        PayServiceRequest request = new PayServiceRequest(
                "111111",
                BigDecimal.valueOf(1000.00),
                "PEN",
                "Pago muy caro",
                "Enel",
                "123"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000.00));

        // When & Then
        assertThatThrownBy(() -> transactionService.payService(request, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Saldo no disponible");
        assertThat(sourceAccount.getCurrentBalance()).isEqualByComparingTo("500.00");
    }

    // --- PayInterest test ---

    @Test
    void payInterestSuccess() {
        // Given
        PayInterestRequest request = new PayInterestRequest(
                "222222",
                BigDecimal.valueOf(15.50),
                "PEN",
                "Pago de intereses mensuales"
        );

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("222222"))
                .thenReturn(Optional.of(targetAccount));
        when(currencyRepository.findByCode("PEN"))
                .thenReturn(Optional.of(currencyPEN));
        when(currencyExchangeService.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(15.50));

        TransactionType transactionType = TransactionType.builder().name("PAGO_INTERESES").build();
        when(transactionTypeRepository.findById(5)).thenReturn(Optional.of(transactionType));

        // When
        TransactionResponse response = transactionService.payInterest(request);

        // Then
        assertThat(response.targetAccount()).isEqualTo("222222");
        assertThat(response.transactionType()).isEqualTo("PAGO_INTERESES");
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(15.50));
        assertThat(targetAccount.getCurrentBalance()).isEqualByComparingTo("615.50");
        verify(bankTransactionRepository).save(any(BankTransaction.class));
    }

    // --- GetHistory test ---

    @Test
    void getHistorySuccess() {
        // Given
        BankTransaction transaction = BankTransaction.builder()
                .id(1L)
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .transactionType(TransactionType.builder().name("TRANSFERENCIA").build())
                .transactionStatus(statusSuccess)
                .currency(currencyPEN)
                .amount(BigDecimal.valueOf(1999.00))
                .build();

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));
        when(bankTransactionRepository.findAllByAccountId(any()))
                .thenReturn(List.of(transaction));

        // When
        List<TransactionResponse> result = transactionService.getHistory("111111", 1L);

        // Then
        TransactionResponse response = result.get(0);
        assertThat(result).hasSize(1);
        assertThat(response.sourceAccount()).isEqualTo("111111");
        assertThat(response.targetAccount()).isEqualTo("222222");
        assertThat(response.transactionType()).isEqualTo("TRANSFERENCIA");
        assertThat(response.amount()).isEqualByComparingTo("1999.00");
    }

    @Test
    void getHistory_AccessDenied_NotOwner() {
        // Given
        String accountNumber = "111111";

        when(bankAccountRepository.findByAccountNumberAndIsActiveTrue("111111"))
                .thenReturn(Optional.of(sourceAccount));

        // When & Then
        assertThatThrownBy(() -> transactionService.getHistory("111111", 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La cuenta origen no te pertenece");
        verify(bankTransactionRepository, never()).findAllByAccountId(any());
    }
}