package com.example.bank_app.service;

import com.example.bank_app.dto.account.AccountCreationRequest;
import com.example.bank_app.dto.account.AccountResponse;
import com.example.bank_app.model.User;

import java.util.List;

public interface AccountService {
    List<AccountResponse> getAccountsByUserId(Long userId);
    AccountResponse createAccount(AccountCreationRequest request, Long userId);
    AccountResponse createAccount(AccountCreationRequest request, User user);
}
