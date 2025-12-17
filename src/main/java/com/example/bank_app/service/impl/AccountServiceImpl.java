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
import com.example.bank_app.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {
    private final AccountTypeRepository accountTypeRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CurrencyRepository currencyRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return bankAccountRepository.findAllByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(bankAccount -> new AccountResponse(
                        bankAccount.getId(),
                        bankAccount.getCurrency().getName(),
                        bankAccount.getCurrentBalance(),
                        bankAccount.getAccountType().getName(),
                        bankAccount.getAccountNumber()
                ))
                .toList();
    }

    public AccountResponse createAccount(AccountCreationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        return createAccount(request, user);
    }

    @Override
    public AccountResponse createAccount(AccountCreationRequest request, User user) {
        Currency currency = currencyRepository.findByCode(request.currency())
                .orElseThrow(() -> new RuntimeException("Código de moneda no encontrada en la base de datos."));

        AccountType accountType = accountTypeRepository.findByName(request.accountType())
                .orElseThrow(() -> new RuntimeException("Tipo de cuenta no encontrada en la base de datos."));

        BankAccount bankAccount = BankAccount.builder()
                .user(user)
                .accountType(accountType)
                .accountNumber(generateAccountNumber())
                .currency(currency)
                .build();

        BankAccount saved = bankAccountRepository.save(bankAccount);

        return new AccountResponse(
                saved.getId(),
                saved.getCurrency().getName(),
                saved.getCurrentBalance(),
                saved.getAccountType().getName(),
                saved.getAccountNumber()
        );
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = generateRandomDigits(14);

        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
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
