package com.example.bank_app.service.impl;

import com.example.bank_app.dto.transaction.*;
import com.example.bank_app.model.*;
import com.example.bank_app.repository.*;
import com.example.bank_app.service.CurrencyExchangeService;
import com.example.bank_app.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final BankAccountRepository bankAccountRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyExchangeService currencyExchangeService;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionStatusRepository transactionStatusRepository;
    private final BankTransactionRepository bankTransactionRepository;

    @Override
    public TransactionResponse transfer(TransferRequest request, Long userId) {
        BankAccount sourceAccount = bankAccountRepository.findByAccountNumberAndIsActiveTrue(request.sourceAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta origen no disponible"));

        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("La cuenta origen no te pertenece");
        }

        BankAccount targetAccount = bankAccountRepository.findByAccountNumberAndIsActiveTrue(request.targetAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta destino no disponible"));

        Currency currency = currencyRepository.findByCode(request.currency())
                .orElseThrow(() -> new RuntimeException("Tipo de moneda no soportada"));

        BigDecimal amountToSubtract = currencyExchangeService.convert(
                request.amount(),
                currency.getCode(),
                sourceAccount.getCurrency().getCode()
        );

        BigDecimal amountToAdd = currencyExchangeService.convert(
                request.amount(),
                currency.getCode(),
                targetAccount.getCurrency().getCode()
        );

        if (sourceAccount.getCurrentBalance().compareTo(amountToSubtract) < 0) {
            throw new RuntimeException("Saldo no disponible");
        }

        sourceAccount.setCurrentBalance(sourceAccount.getCurrentBalance().subtract(amountToSubtract));
        targetAccount.setCurrentBalance(targetAccount.getCurrentBalance().add(amountToAdd));

        TransactionType transactionType = transactionTypeRepository.findById(1) // TRANSFERENCIA
                .orElseThrow(() -> new RuntimeException("Tipo de transacción no existente"));

        TransactionStatus transactionStatus = transactionStatusRepository.findById(2) // SUCCESS
                .orElseThrow(() -> new RuntimeException("Estado de transacción no encontrado"));

        BankTransaction bankTransaction = BankTransaction.builder()
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .transactionType(transactionType)
                .amount(request.amount())
                .currency(currency)
                .transactionStatus(transactionStatus)
                .referenceCode(generateReferenceCode())
                .description(request.description())
                .build();

        BankTransaction saved = bankTransactionRepository.save(bankTransaction);

        return mapToTransactionResponse(saved);
    }

    @Override
    public TransactionResponse deposit(DepositRequest request) {
        BankAccount targetAccount = bankAccountRepository.findByAccountNumberAndIsActiveTrue(request.targetAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta destino no disponible"));

        Currency currency = currencyRepository.findByCode(request.currency())
                .orElseThrow(() -> new RuntimeException("Tipo de moneda no soportada"));

        BigDecimal amountToAdd = currencyExchangeService.convert(
                request.amount(),
                currency.getCode(),
                targetAccount.getCurrency().getCode()
        );

        targetAccount.setCurrentBalance(targetAccount.getCurrentBalance().add(amountToAdd));

        TransactionType transactionType = transactionTypeRepository.findById(2) // DEPOSITO
                .orElseThrow(() -> new RuntimeException("Tipo de transacción no existente"));

        TransactionStatus transactionStatus = transactionStatusRepository.findById(2) // SUCCESS
                .orElseThrow(() -> new RuntimeException("Estado de transacción no encontrado"));

        BankTransaction bankTransaction = BankTransaction.builder()
                .sourceAccount(null)
                .targetAccount(targetAccount)
                .transactionType(transactionType)
                .amount(request.amount())
                .currency(currency)
                .transactionStatus(transactionStatus)
                .referenceCode(generateReferenceCode())
                .description(request.description())
                .build();

        BankTransaction saved = bankTransactionRepository.save(bankTransaction);

        return mapToTransactionResponse(saved);
    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request, Long userId) {
        BankAccount sourceAccount = bankAccountRepository.findByAccountNumberAndIsActiveTrue(request.sourceAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta origen no disponible"));

        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("La cuenta origen no te pertenece");
        }

        Currency currency = currencyRepository.findByCode(request.currency())
                .orElseThrow(() -> new RuntimeException("Tipo de moneda no soportada"));

        BigDecimal amountToSubtract = currencyExchangeService.convert(
                request.amount(),
                currency.getCode(),
                sourceAccount.getCurrency().getCode()
        );

        if (sourceAccount.getCurrentBalance().compareTo(amountToSubtract) < 0) {
            throw new RuntimeException("Saldo no disponible");
        }

        sourceAccount.setCurrentBalance(sourceAccount.getCurrentBalance().subtract(amountToSubtract));

        TransactionType transactionType = transactionTypeRepository.findById(3) // RETIRO
                .orElseThrow(() -> new RuntimeException("Tipo de transacción no existente"));

        TransactionStatus transactionStatus = transactionStatusRepository.findById(2) // SUCCESS
                .orElseThrow(() -> new RuntimeException("Estado de transacción no encontrado"));

        BankTransaction bankTransaction = BankTransaction.builder()
                .sourceAccount(sourceAccount)
                .targetAccount(null)
                .transactionType(transactionType)
                .amount(request.amount())
                .currency(currency)
                .transactionStatus(transactionStatus)
                .referenceCode(generateReferenceCode())
                .description(request.description())
                .build();

        BankTransaction saved = bankTransactionRepository.save(bankTransaction);

        return mapToTransactionResponse(saved);
    }

    @Override
    public TransactionResponse payService(PayServiceRequest request, Long userId) {
        BankAccount sourceAccount = bankAccountRepository.findByAccountNumberAndIsActiveTrue(request.sourceAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta origen no disponible"));

        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new RuntimeException("La cuenta origen no te pertenece");
        }

        Currency currency = currencyRepository.findByCode(request.currency())
                .orElseThrow(() -> new RuntimeException("Tipo de moneda no soportada"));

        BigDecimal amountToSubtract = currencyExchangeService.convert(
                request.amount(),
                currency.getCode(),
                sourceAccount.getCurrency().getCode()
        );

        if (sourceAccount.getCurrentBalance().compareTo(amountToSubtract) < 0) {
            throw new RuntimeException("Saldo no disponible");
        }

        sourceAccount.setCurrentBalance(sourceAccount.getCurrentBalance().subtract(amountToSubtract));

        TransactionType transactionType = transactionTypeRepository.findById(4) // PAGO_SERVICIO
                .orElseThrow(() -> new RuntimeException("Tipo de transacción no existente"));

        TransactionStatus transactionStatus = transactionStatusRepository.findById(2) // SUCCESS
                .orElseThrow(() -> new RuntimeException("Estado de transacción no encontrado"));

        String userDescription = request.description() != null ? request.description() : "";

        String requestDescription = "SERVICIO: " + request.serviceName()
                + "\nSUMINISTRO: " + request.supplyCode()
                + "\n" + userDescription;

        BankTransaction bankTransaction = BankTransaction.builder()
                .sourceAccount(sourceAccount)
                .targetAccount(null)
                .transactionType(transactionType)
                .amount(request.amount())
                .currency(currency)
                .transactionStatus(transactionStatus)
                .referenceCode(generateReferenceCode())
                .description(requestDescription)
                .build();

        BankTransaction saved = bankTransactionRepository.save(bankTransaction);

        return mapToTransactionResponse(saved);
    }

    @Override
    public TransactionResponse payInterest(PayInterestRequest request) {
        BankAccount targetAccount = bankAccountRepository.findByAccountNumberAndIsActiveTrue(request.targetAccount())
                .orElseThrow(() -> new RuntimeException("Cuenta destino no disponible"));

        Currency currency = currencyRepository.findByCode(request.currency())
                .orElseThrow(() -> new RuntimeException("Tipo de moneda no soportada"));

        BigDecimal amountToAdd = currencyExchangeService.convert(
                request.amount(),
                currency.getCode(),
                targetAccount.getCurrency().getCode()
        );

        targetAccount.setCurrentBalance(targetAccount.getCurrentBalance().add(amountToAdd));

        TransactionType transactionType = transactionTypeRepository.findById(5) // PAGO_INTERESES
                .orElseThrow(() -> new RuntimeException("Tipo de transacción no existente"));

        TransactionStatus transactionStatus = transactionStatusRepository.findById(2) // SUCCESS
                .orElseThrow(() -> new RuntimeException("Estado de transacción no encontrado"));

        BankTransaction bankTransaction = BankTransaction.builder()
                .sourceAccount(null)
                .targetAccount(targetAccount)
                .transactionType(transactionType)
                .amount(request.amount())
                .currency(currency)
                .transactionStatus(transactionStatus)
                .referenceCode(generateReferenceCode())
                .description(request.description())
                .build();

        BankTransaction saved = bankTransactionRepository.save(bankTransaction);

        return mapToTransactionResponse(saved);
    }

    @Override
    public List<TransactionResponse> getHistory(String accountNumber, Long userId) {
        BankAccount account = bankAccountRepository.findByAccountNumberAndIsActiveTrue(accountNumber)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no disponible"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("La cuenta origen no te pertenece");
        }

        return bankTransactionRepository.findAllByAccountId(account.getId())
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    // Métodos privados

    private TransactionResponse mapToTransactionResponse(BankTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSourceAccount() != null ? transaction.getSourceAccount().getAccountNumber() : "EXTERNO",
                transaction.getTargetAccount() != null ? transaction.getTargetAccount().getAccountNumber() : "EXTERNO/VENTANILLA",
                transaction.getTransactionType().getName(),
                transaction.getAmount(),
                transaction.getTransactionStatus().getName(),
                transaction.getDescription(),
                transaction.getReferenceCode(),
                transaction.getCurrency().getCode(),
                transaction.getCreatedAt()
        );
    }

    private String generateReferenceCode() {
        String referenceCode;
        do {
            referenceCode = generateRandomDigits(10);

        } while (bankTransactionRepository.existsByReferenceCode(referenceCode));

        return referenceCode;
    }

    private String generateRandomDigits(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
